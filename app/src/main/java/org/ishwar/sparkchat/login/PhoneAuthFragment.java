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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import org.ishwar.sparkchat.R;
import org.ishwar.sparkchat.databinding.FragmentPhoneAuthBinding;
import org.ishwar.sparkchat.dialog.SparkDialogSheet;
import org.ishwar.sparkchat.dialog.TermsAndPrivacySheetFragment;
import org.ishwar.sparkchat.firebase.FirebaseUtils;
import org.ishwar.sparkchat.fragments.BaseFragment;
import org.ishwar.sparkchat.utils.AndroidUtils;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class PhoneAuthFragment extends BaseFragment implements View.OnClickListener {
    private FragmentPhoneAuthBinding binding;

    private boolean isOtpView;

    private OnCompleteListener<AuthResult> mAuthCompletionCallbacks = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful() && FirebaseUtils.isLoggedIn()) {
                getAuthenticationActivity().loadStagePush();
            } else {
                switchOtpView();
                SparkDialogSheet dialog_sheet = SparkDialogSheet.make(getContext());
                Exception exception = task.getException();
                if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                    dialog_sheet.addMessage(getString(R.string.warning_error_invalid_otp));
                } else {
                    dialog_sheet.addMessage(getString(R.string.warning_error_signing_in));
                }
                dialog_sheet.addDivider();
                dialog_sheet.addButton(-1, getString(R.string.label_got_it), null);
                dialog_sheet.show();
            }
        }
    };

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mAuthCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            getAuthenticationActivity().getOldSessionData().edit().putString(AuthenticationActivity.KEY_AUTHENTICATION_VERIFICATION_ID, s).apply();
            getAuthenticationActivity().getOldSessionData().edit().putLong(AuthenticationActivity.KEY_LAST_AUTHENTICATION_START_TIME, System.currentTimeMillis()).apply();
            switchOtpView();
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
            super.onCodeAutoRetrievalTimeOut(s);
            binding.bttnBack.setVisibility(View.VISIBLE);
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            binding.bttnLogin.startProgress(getString(R.string.label_verifying));
            binding.edLoginInput.setText(phoneAuthCredential.getSmsCode());

            FirebaseUtils.getAuth().signInWithCredential(phoneAuthCredential).addOnCompleteListener(getAuthenticationActivity(), mAuthCompletionCallbacks);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            switchPhoneView();
            SparkDialogSheet dialog_sheet = SparkDialogSheet.make(getContext());
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                dialog_sheet.addMessage(getString(R.string.warning_error_invalid_phone_number));
            } else if (e instanceof FirebaseTooManyRequestsException) {
                dialog_sheet.addMessage(getString(R.string.warning_error_verification_quota_limit) + ", " + e);
            } else {
                dialog_sheet.addMessage(getString(R.string.warning_error_verification_failed));
            }
            dialog_sheet.addDivider();
            dialog_sheet.addButton(-1, getString(R.string.label_got_it), null);
            dialog_sheet.show();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPhoneAuthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View parent, Bundle savedInstanceState) {
        super.onViewCreated(parent, savedInstanceState);
        
        if (getAuthenticationActivity().getOldSessionData().contains(AuthenticationActivity.KEY_AUTHENTICATION_VERIFICATION_ID) && AuthenticationActivity.AUTHENTICATION_RESEND_OTP_DURATION >= System.currentTimeMillis() - getAuthenticationActivity().getOldSessionData().getLong(AuthenticationActivity.KEY_LAST_AUTHENTICATION_START_TIME, 0)) {
            switchOtpView();
        } else {
            switchPhoneView();
        }

        CountryCodeSheet mCountryCodeSheet = new CountryCodeSheet(getContext(), binding.tvCountryCode);
        binding.tvCountryCode.setOnClickListener(mCountryCodeSheet);

        binding.bttnLogin.setOnClickListener(this);
        binding.bttnBack.setOnClickListener(this);
        binding.tvViewTermAndConditions.setOnClickListener(this);

        binding.edLoginInput.setOnEditorActionListener((p1, p2, p3) -> {
            onClick(binding.bttnLogin);
            return false;
        });
    }

    private void switchPhoneView() {
        isOtpView = false;
        if (getAuthenticationActivity().getOldSessionData().contains(AuthenticationActivity.KEY_LAST_AUTHENTICATION_START_TIME)) {
            getAuthenticationActivity().getOldSessionData().edit().remove(AuthenticationActivity.KEY_LAST_AUTHENTICATION_START_TIME).apply();
        }
        if (getAuthenticationActivity().getOldSessionData().contains(AuthenticationActivity.KEY_AUTHENTICATION_VERIFICATION_ID)) {
            getAuthenticationActivity().getOldSessionData().edit().remove(AuthenticationActivity.KEY_AUTHENTICATION_VERIFICATION_ID).apply();
        }
        binding.tvCountryCode.setText(getAuthenticationActivity().getOldSessionData().getString(AuthenticationActivity.KEY_AUTHENTICATION_COUNTRY_CODE, getString(R.string.default_country_code)));
        binding.edLoginInput.setText(getAuthenticationActivity().getOldSessionData().getString(AuthenticationActivity.KEY_AUTHENTICATION_PHONE, ""));
        binding.edLoginInput.setSelection(binding.edLoginInput.getText().length());

        binding.bttnLogin.stopProgressForcibly();
        binding.bttnLogin.setText(getString(R.string.label_continue));
        binding.tvLoginPrompt.setText(R.string.label_phone_number);
        binding.edLoginInput.setHint(R.string.label_number);
        binding.edLoginInput.setInputType(InputType.TYPE_CLASS_PHONE);
        binding.bttnBack.setVisibility(View.GONE);
        binding.tvCountryCode.setVisibility(View.VISIBLE);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) binding.edLoginInput.getLayoutParams();
        layoutParams.leftMargin = 0;
        binding.edLoginInput.requestLayout();
    }

    private void switchOtpView() {
        isOtpView = true;
        binding.bttnLogin.stopProgressForcibly();
        binding.bttnLogin.setText(getString(R.string.label_verify));
        String number = getAuthenticationActivity().getOldSessionData().getString(AuthenticationActivity.KEY_AUTHENTICATION_COUNTRY_CODE, getString(R.string.default_country_code)) + getAuthenticationActivity().getOldSessionData().getString(AuthenticationActivity.KEY_AUTHENTICATION_PHONE, "");
        binding.tvLoginPrompt.setText(getString(R.string.warning_otp, number));
        binding.edLoginInput.setText("");
        binding.edLoginInput.setHint(R.string.label_verification_code);
        binding.edLoginInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.bttnBack.setVisibility(View.GONE);
        binding.tvCountryCode.setVisibility(View.GONE);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) binding.edLoginInput.getLayoutParams();
        layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.secondary_padding);
        binding.edLoginInput.requestLayout();
    }

    @SuppressLint("ResourceType")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bttn_login && !isOtpView) {
            String phone_number = binding.edLoginInput.getText().toString().trim();
            if (Patterns.PHONE.matcher(phone_number).matches()) {
                SparkDialogSheet dialog_sheet = SparkDialogSheet.make(getActivity());
                dialog_sheet.addMessage(getString(R.string.warning_term_and_conditions));
                dialog_sheet.addDivider();
                dialog_sheet.addButton(R.string.label_accept, getString(R.string.label_accept), this);
                dialog_sheet.addDivider();
                dialog_sheet.addButton(R.string.label_contact_support, getString(R.string.label_contact_support), ContextCompat.getColor(getContext(), R.color.secondaryTextColor), this);
                dialog_sheet.addDivider();
                dialog_sheet.addButton(R.string.label_read_more, getString(R.string.label_read_more), ContextCompat.getColor(getContext(), R.color.secondaryTextColor), this);
                dialog_sheet.show();
            } else {
                SparkDialogSheet dialog_sheet = SparkDialogSheet.make(getActivity());
                dialog_sheet.addMessage(getString(R.string.warning_error_invalid_phone_number));
                dialog_sheet.addDivider();
                dialog_sheet.addButton(-1, getString(R.string.label_got_it), null);
                dialog_sheet.show();
            }
        }

        if (v.getId() == R.id.bttn_login && isOtpView) {
            String OTP = binding.edLoginInput.getText().toString().trim();
            if (OTP.length() >= getResources().getInteger(R.integer.minimum_otp_length) && getResources().getInteger(R.integer.maximum_otp_length) >= OTP.length()) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(getAuthenticationActivity().getOldSessionData().getString(AuthenticationActivity.KEY_AUTHENTICATION_VERIFICATION_ID, ""), OTP);
                mAuthCallbacks.onVerificationCompleted(credential);
            } else {
                SparkDialogSheet dialog_sheet = SparkDialogSheet.make(getActivity());
                dialog_sheet.addMessage(getString(R.string.warning_error_invalid_otp));
                dialog_sheet.addDivider();
                dialog_sheet.addButton(-1, getString(R.string.label_got_it), null);
                dialog_sheet.show();
            }
        }
        if (v.getId() == R.string.label_accept) {
            SparkDialogSheet.destroy();
            binding.bttnLogin.startProgress(getString(R.string.status_otp_sending));

            String phone_number = binding.edLoginInput.getText().toString().trim();
            getAuthenticationActivity().getOldSessionData().edit().putString(AuthenticationActivity.KEY_AUTHENTICATION_COUNTRY_CODE, binding.tvCountryCode.getText().toString()).apply();
            getAuthenticationActivity().getOldSessionData().edit().putString(AuthenticationActivity.KEY_AUTHENTICATION_PHONE, phone_number).apply();

            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseUtils.getAuth())
                    .setPhoneNumber(binding.tvCountryCode.getText().toString().concat(phone_number))       // Phone number to verify
                    .setTimeout(AuthenticationActivity.AUTHENTICATION_RESEND_OTP_DURATION, TimeUnit.MILLISECONDS) // Timeout and unit
                    .setActivity(getAuthenticationActivity())                 // Activity (for callback binding)
                    .setCallbacks(mAuthCallbacks)          // OnVerificationStateChangedCallbacks
                    .build();

            PhoneAuthProvider.verifyPhoneNumber(options);
        }

        if (v.getId() == R.string.label_read_more || v.getId() == R.id.tv_view_term_and_conditions) {
            new TermsAndPrivacySheetFragment().show(getActivity().getSupportFragmentManager());
        }

        if (v.getId() == R.string.label_contact_support) {
            SparkDialogSheet.destroy();
            AndroidUtils.composeGmail(getContext(), getString(R.string.app_developer_gmail_address), getString(R.string.app_name), "");
        }

        if (v.getId() == R.id.bttn_back) {
            getAuthenticationActivity().getOldSessionData().edit().remove(AuthenticationActivity.KEY_AUTHENTICATION_VERIFICATION_ID).apply();
            switchPhoneView();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (!isOtpView) {
            SharedPreferences.Editor editor = getAuthenticationActivity().getOldSessionData().edit();
            editor.putString(AuthenticationActivity.KEY_AUTHENTICATION_COUNTRY_CODE, binding.tvCountryCode.getText().toString()).apply();
            editor.putString(AuthenticationActivity.KEY_AUTHENTICATION_PHONE, binding.edLoginInput.getText().toString()).apply();
            editor.apply();
        }
    }
}
