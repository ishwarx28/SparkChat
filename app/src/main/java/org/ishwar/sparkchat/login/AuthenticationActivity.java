/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import org.ishwar.sparkchat.R;
import org.ishwar.sparkchat.databinding.ActivityAuthenticationBinding;
import org.ishwar.sparkchat.firebase.FirebaseUtils;
import org.ishwar.sparkchat.fragments.BaseFragment;
import org.ishwar.sparkchat.home.HomeActivity;

public class AuthenticationActivity extends AppCompatActivity {

	public static final String KEY_AUTHENTICATION_STAGE = "authentication_stage";
    public static final String AUTHENTICATION_STAGE_PHONE_AUTH = "authentication_phone_auth";
    public static final String AUTHENTICATION_STAGE_PUSH = "authentication_push";
    public static final String AUTHENTICATION_STAGE_SUCCESS = "authentication_success";

    public static final String KEY_AUTHENTICATION_VERIFICATION_ID = "authentication_verification_id";
    public static final String KEY_LAST_AUTHENTICATION_START_TIME = "authentication_start_time";
    public static final long AUTHENTICATION_RESEND_OTP_DURATION = 60000;
    public static String KEY_AUTHENTICATION_COUNTRY_CODE = "authentication_country_code";
    public static String KEY_AUTHENTICATION_PHONE = "authentication_phone";

    private ActivityAuthenticationBinding binding;

    private SharedPreferences oldSessionData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		oldSessionData = getSharedPreferences(AuthenticationActivity.class.getSimpleName(), Context.MODE_PRIVATE);

        if(AUTHENTICATION_STAGE_SUCCESS.equals(oldSessionData.getString(KEY_AUTHENTICATION_STAGE, AUTHENTICATION_STAGE_PHONE_AUTH)) && FirebaseUtils.isLoggedIn()){
            loadStageSuccess();
            return;
        }

        binding = ActivityAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

		setSupportActionBar(binding.toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

    }
	
	public void setToolbarTitle(CharSequence title){
        this.binding.labelToolbar.setText(title);
    }
    
    public void hideAppbar(){
        this.binding.appbar.setVisibility(View.GONE);
    }
    
    public void showAppbar(){
        this.binding.appbar.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void onStart(){
        super.onStart();
        String stage = oldSessionData.getString(KEY_AUTHENTICATION_STAGE, AUTHENTICATION_STAGE_PHONE_AUTH);
        switch (stage) {
            case AUTHENTICATION_STAGE_PHONE_AUTH:
                loadStagePhoneAuth();
                break;
            case AUTHENTICATION_STAGE_PUSH:
                loadStagePush();
                break;
            case AUTHENTICATION_STAGE_SUCCESS:
                loadStageSuccess();
                break;
        }
    }
    
    public SharedPreferences getOldSessionData(){
        return oldSessionData;
    }
    
    public void openFragment(String name, boolean animate) {
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(name);
        if(fragment == null){
            try{
                fragment = (BaseFragment) Class.forName(name).newInstance();
            }catch (Exception e){
                throw new RuntimeException(e.getLocalizedMessage(), e);
            }
        }
        if(animate){
            trans.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        trans.replace(R.id.container, fragment, name);
        trans.commitNow();
    }
    
    public void loadStagePhoneAuth(){
        oldSessionData.edit().putString(KEY_AUTHENTICATION_STAGE, AUTHENTICATION_STAGE_PHONE_AUTH).apply();
        openFragment(PhoneAuthFragment.class.getCanonicalName(), false);
    }
    
    public void loadStagePush(){
        oldSessionData.edit().putString(KEY_AUTHENTICATION_STAGE, AUTHENTICATION_STAGE_PUSH).apply();
        openFragment(PushFragment.class.getCanonicalName(), true);
    }

    public void loadStageSuccess(){
        oldSessionData.edit().remove(KEY_AUTHENTICATION_VERIFICATION_ID).remove(KEY_LAST_AUTHENTICATION_START_TIME).putString(KEY_AUTHENTICATION_STAGE, AUTHENTICATION_STAGE_SUCCESS).apply();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed(){
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        
        if(fragment == null || !fragment.onBackPressed()){
            super.onBackPressed();
        }
    }
    
    
}