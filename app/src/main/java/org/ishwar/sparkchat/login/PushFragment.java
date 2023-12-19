/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat.login;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.ishwar.sparkchat.R;
import org.ishwar.sparkchat.databinding.FragmentPushBinding;
import org.ishwar.sparkchat.dialog.SparkDialogSheet;
import org.ishwar.sparkchat.firebase.FirebaseUtils;
import org.ishwar.sparkchat.fragments.BaseFragment;
import org.ishwar.sparkchat.models.SparkUser;
import org.ishwar.sparkchat.models.SparkUserMetadata;
import org.jetbrains.annotations.NotNull;

public class PushFragment extends BaseFragment
{
    private FirebaseUser firebaseuser;

    private FragmentPushBinding binding;

    private OnFailureListener failureListener = e -> {
        SparkDialogSheet dialog = SparkDialogSheet.make(PushFragment.this.getActivity());
        dialog.setCancelable(false);

        StringBuilder message = new StringBuilder();
        message.append(PushFragment.this.getString(R.string.warning_error_unexpected));
        message.append(" ");
        message.append(e.getLocalizedMessage());
        dialog.addMessage(message);

        @SuppressLint("ResourceType") View.OnClickListener listener = p1 -> {
            if (R.string.label_exit == p1.getId()) {
                PushFragment.this.getAuthenticationActivity().finishAffinity();
            }

            if (R.string.label_report == p1.getId()) {
                throw new RuntimeException(e);
            }
        };

        dialog.addDivider();
        dialog.addButton(R.string.label_report, PushFragment.this.getString(R.string.label_report), listener);
        dialog.addDivider();
        dialog.addButton(R.string.label_exit, PushFragment.this.getString(R.string.label_exit), ContextCompat.getColor(PushFragment.this.getContext(), R.color.secondaryTextColor), listener);
        dialog.show();
    };

    private ValueEventListener metaListener = new ValueEventListener(){
        @Override
        public void onDataChange(@NonNull DataSnapshot p1){
            if(p1.exists()){
                getAuthenticationActivity().loadStageSuccess();
            }else{
                binding.tvPushUpdate.startProgress(getString(R.string.status_user_updating_metadata));

                long creationTime = firebaseuser.getMetadata() != null ? firebaseuser.getMetadata().getCreationTimestamp() : System.currentTimeMillis();
                String countryCode = getAuthenticationActivity().getOldSessionData().getString(AuthenticationActivity.KEY_AUTHENTICATION_COUNTRY_CODE, getString(R.string.default_country_code));
                String phone = getAuthenticationActivity().getOldSessionData().getString(AuthenticationActivity.KEY_AUTHENTICATION_PHONE, "");
                SparkUserMetadata metadata = new SparkUserMetadata(null, countryCode, phone, creationTime);

                p1.getRef().setValue(metadata).addOnSuccessListener(p11 -> getAuthenticationActivity().loadStageSuccess()).addOnFailureListener(failureListener);
            }
        }

        @Override
        public void onCancelled(@NonNull final DatabaseError e){
            failureListener.onFailure(e.toException());
        }
    };
    private TextView loginBttn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        firebaseuser = FirebaseUtils.getAuth().getCurrentUser();
        if(firebaseuser == null){
            getAuthenticationActivity().loadStagePhoneAuth();
            return null;
        }
        binding = FragmentPushBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View parent, Bundle savedInstanceState){
        super.onViewCreated(parent, savedInstanceState);

        binding.edYourName.setOnEditorActionListener((v, actionId, event) -> PushFragment.this.processNameInput());

        loginBttn = parent.findViewById(R.id.bttn_login);

        loginBttn.setOnClickListener(v -> PushFragment.this.processNameInput());

        // Ui Update
        getAuthenticationActivity().hideAppbar();
        binding.layoutPushview.setVisibility(View.VISIBLE);
        loginBttn.setVisibility(View.GONE);
        binding.tvPushUpdate.startProgress(getString(R.string.status_connecting));

        ValueEventListener userListener = new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot p1){
                if(p1.exists()){
                    FirebaseUtils.getMetadataRef().child(firebaseuser.getUid()).addListenerForSingleValueEvent(metaListener);
                }else{
                    getAuthenticationActivity().showAppbar();
                    binding.layoutPushview.setVisibility(View.GONE);
                    loginBttn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError p1){
                failureListener.onFailure(p1.toException());
            }
        };

        FirebaseUtils.getUserRef().child(firebaseuser.getUid()).addListenerForSingleValueEvent(userListener);
    }

    private boolean processNameInput() {
        String name = binding.edYourName.getText().toString().trim();
        if(name.length() == 0){
            return true;
        }

        getAuthenticationActivity().hideAppbar();
        binding.layoutPushview.setVisibility(View.VISIBLE);
        loginBttn.setVisibility(View.GONE);
        binding.tvPushUpdate.startProgress(getString(R.string.status_user_updating));

        StringBuilder finalName = new StringBuilder();
        String[] parts = name.split("\\s+");
        for(int i = 0; i < parts.length; ++i){
            finalName.append(i == 0 ? 0 : ' ').append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].length() > 1 ? parts[i].substring(1).toLowerCase() : "");
        }

        SparkUser user = new SparkUser(null, finalName.toString(), null);

        FirebaseUtils.getUserRef().child(FirebaseUtils.getUid()).setValue(user).addOnSuccessListener(p1 -> FirebaseUtils.getMetadataRef().child(firebaseuser.getUid()).addListenerForSingleValueEvent(metaListener)).addOnFailureListener(failureListener);
        return false;
    }


    @Override
    public void updateToolbar(){
        getAuthenticationActivity().setToolbarTitle(getString(R.string.label_your_name));
    }
}