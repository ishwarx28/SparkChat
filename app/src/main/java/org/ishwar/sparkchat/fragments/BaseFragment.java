/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.ishwar.sparkchat.home.HomeActivity;
import org.ishwar.sparkchat.login.AuthenticationActivity;

public class BaseFragment extends Fragment {
	
  @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        
        updateToolbar();
    }

    public AuthenticationActivity getAuthenticationActivity(){
        return getActivity() instanceof AuthenticationActivity ? (AuthenticationActivity) getActivity() : null;
    }

    public HomeActivity getHomeActivity(){
        return getActivity() instanceof HomeActivity ? (HomeActivity) getActivity() : null;
    }

    public void updateToolbar(){

    }

    public boolean onBackPressed(){
        return false;
    }
}