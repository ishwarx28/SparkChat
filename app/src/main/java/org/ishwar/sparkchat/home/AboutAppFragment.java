/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.navigation.NavigationView;

import org.ishwar.sparkchat.R;
import org.ishwar.sparkchat.databinding.ItemAboutAppToolbarBinding;
import org.ishwar.sparkchat.dialog.TermsAndPrivacySheetFragment;
import org.ishwar.sparkchat.fragments.BaseFragment;
import org.ishwar.sparkchat.utils.AndroidUtils;
import org.ishwar.sparkchat.utils.AppUtils;
import org.jetbrains.annotations.NotNull;

public class AboutAppFragment extends BaseFragment implements NavigationView.OnNavigationItemSelectedListener
{

    private NavigationView mNavAboutApp;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_about_app, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        mNavAboutApp = view.findViewById(R.id.nav_about_app);
        mNavAboutApp.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem p1){

        if(R.id.menu_terms_and_privacy_policy == p1.getItemId()){
            new TermsAndPrivacySheetFragment().show(getActivity().getSupportFragmentManager());
        }

        if(R.id.menu_feedback == p1.getItemId()){
            AndroidUtils.composeGmail(getContext(), getString(R.string.app_developer_gmail_address), "[" + p1.getTitle() + "]:v" + getString(R.string.app_version), null);
        }

        if(R.id.menu_report_issue == p1.getItemId()){
            AndroidUtils.composeGmail(getContext(), getString(R.string.app_developer_gmail_address), "[" + p1.getTitle() + "]:v" + getString(R.string.app_version), null);
        }

        if(R.id.menu_share_app == p1.getItemId()){
            AppUtils.shareAppUrl(getContext());
        }

        return false;
    }

    @Override
    public void updateToolbar(){
        ViewGroup parent = getHomeActivity().getToolbarContainer();
        parent.removeAllViews();
        ItemAboutAppToolbarBinding.inflate(getLayoutInflater(), parent, true);
    }
}
