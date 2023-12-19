/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.ishwar.sparkchat.R;
import org.ishwar.sparkchat.utils.AndroidUtils;
import org.ishwar.sparkchat.utils.AppUtils;

public class TermsAndPrivacySheetFragment extends BottomSheetDialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_terms_and_privacy_policy, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView mTvTermsAndPrivacyPolicy = view.findViewById(R.id.tv_terms_and_privacy_policy);
        mTvTermsAndPrivacyPolicy.setText(HtmlCompat.fromHtml(AndroidUtils.readAsset(getContext(), AppUtils.PATH_TERM_AND_PRIVACY_POLICY), HtmlCompat.FROM_HTML_MODE_LEGACY));

        view.findViewById(R.id.dismiss_dialog_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        view.findViewById(R.id.contact_developer_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.composeGmail(getContext(), getString(R.string.app_developer_gmail_address), "[" + getString(R.string.app_name) + "]:v" + getString(R.string.app_version), null);
            }
        });
    }

    public void show(FragmentManager manager) {
        super.show(manager, getClass().getSimpleName());
    }
}