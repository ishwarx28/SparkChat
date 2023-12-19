/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat.home;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.slidingpanelayout.widget.SlidingPaneLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.ishwar.sparkchat.R;
import org.ishwar.sparkchat.databinding.ActivtyHomeBinding;
import org.ishwar.sparkchat.firebase.FirebaseUtils;
import org.ishwar.sparkchat.fragments.BaseFragment;
import org.ishwar.sparkchat.models.SparkUser;
import org.ishwar.sparkchat.utils.AndroidUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements SlidingPaneLayout.PanelSlideListener, View.OnClickListener, ValueEventListener {

    public static final String KEY_LAST_FRAGMENT_TAG = "last_fragment";
    public static final String KEY_LAST_PANEL_STATE = "last_panel_state";

    public static final long DEFAULT_FRAGMENT_LOADING_DELAY = 300L;
    public static final int VIEW_TYPE_1 = 0;
    public static final int VIEW_TYPE_2 = 1;
    public static final int VIEW_TYPE_3 = 2;

    /**
    * Used to store search history
    */
    private SharedPreferences mLocalStore;

    private Handler mGlobalHandler = new Handler(Looper.getMainLooper());

    private List<SparkUser> mSparkUsers = new ArrayList<>();
    private Map<String, DatabaseReference> mRegisteredReferences = new HashMap<>();
    private Map<String, List<OnSuccessListener<SparkUser>>> mRegisteredListeners = new HashMap<>();

    private ActivtyHomeBinding binding;

    private String mLastFragmentTag = null;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!FirebaseUtils.isLoggedIn()){
            finishAffinity();
            return;
        }

        mLocalStore = getSharedPreferences(HomeActivity.class.getSimpleName(), MODE_PRIVATE);

        binding = ActivtyHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();

        FragmentManager mFragmentManager = getSupportFragmentManager();

        // RESTORE FRAGMENT INSTANCES AND STATE, IF NOT AVAILABLE SHOW EMPTY FRAGMENT
        BaseFragment fragment = null;
        boolean restartable = savedInstanceState != null && savedInstanceState.containsKey(KEY_LAST_FRAGMENT_TAG) && (fragment = (BaseFragment) mFragmentManager.findFragmentByTag(savedInstanceState.getString(KEY_LAST_FRAGMENT_TAG))) != null;
        if(restartable){
            openFragment(fragment, savedInstanceState.getString(KEY_LAST_FRAGMENT_TAG), false);
        }else{
            fragment = (BaseFragment) mFragmentManager.findFragmentByTag(AboutAppFragment.class.getCanonicalName());
            if(fragment == null){
                fragment = new AboutAppFragment();
            }
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.replace(R.id.container, fragment, AboutAppFragment.class.getCanonicalName());
            transaction.commit();
        }

        // RESTORE PANE STATE
        if(savedInstanceState != null && savedInstanceState.containsKey(KEY_LAST_PANEL_STATE)){
            binding.slidingPanelLayout.post(() -> {
                if (savedInstanceState.getBoolean(KEY_LAST_PANEL_STATE)) {
                    binding.slidingPanelLayout.openPane();
                    HomeActivity.this.onPanelOpened(null);
                } else {
                    binding.slidingPanelLayout.closePane();
                    HomeActivity.this.onPanelClosed(null);
                }
            });
        }else if(!restartable){
            binding.slidingPanelLayout.openPane();
            onPanelOpened(null);
        }

    }

    private void initialize(){
        this.binding.slidingPanelLayout.setSliderFadeColor(ContextCompat.getColor(this, R.color.sliding_fade_color));
        this.binding.slidingPanelLayout.setPanelSlideListener(this);

        setSupportActionBar(binding.homeToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setHomeButtonEnabled(false);
        }

        this.binding.backButton.setOnClickListener(this);
        this.binding.menuButton.setOnClickListener(this);
        this.binding.addPersonButton.setOnClickListener(this);
    }

    public SharedPreferences getLocalStore(){
        return mLocalStore;
    }

    public Handler getGlobalHandler() {
        return mGlobalHandler;
    }

    public ViewGroup getToolbarContainer() {
        return binding.toolbarContainer;
    }

    public void openChatFragment(String senderUid, boolean addToBackStack){
        FragmentManager mFragmentManager = getSupportFragmentManager();
        ChatFragment fragment = (ChatFragment) mFragmentManager.findFragmentByTag(senderUid);
        if(fragment == null){
            fragment = new ChatFragment();
        }
        openFragment(fragment, senderUid, addToBackStack);
    }

    public void openFragment(String tag, boolean addToBackStack){
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if(fragment == null){
            try{
                fragment = (BaseFragment) Class.forName(tag).newInstance();
            }catch(Exception e){
                throw new RuntimeException("\"" + tag + "\" - " + e);
            }
        }
        openFragment(fragment, tag, addToBackStack);
    }

    public void openFragment(BaseFragment fragment, boolean addToBackStack){
        openFragment(fragment, fragment.getClass().getCanonicalName(), addToBackStack);
    }

    public void openFragment(BaseFragment fragment, String tag, boolean addToBackStack){
        this.mLastFragmentTag = tag;
        FragmentManager mFragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment, tag);
        if(addToBackStack){
            transaction.addToBackStack(tag);
        }else if(mFragmentManager.getBackStackEntryCount() > 0){
            FragmentManager.BackStackEntry entry = mFragmentManager.getBackStackEntryAt(0);
            mFragmentManager.popBackStackImmediate(entry.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        transaction.commitNow();

        if(binding.slidingPanelLayout.isOpen()){
            binding.slidingPanelLayout.closePane();
            onPanelClosed(null);
        }
    }

    public void listenToUser(String uid, OnSuccessListener<SparkUser> listener){
        if(!mRegisteredReferences.containsKey(uid)){
            DatabaseReference reference = FirebaseUtils.getUserRef().child(uid);
            mRegisteredReferences.put(uid, reference);
            ArrayList<OnSuccessListener<SparkUser>> newList = new ArrayList<>();
            newList.add(listener);
            mRegisteredListeners.put(uid, newList);
            reference.addValueEventListener(this);
        }else{
            List<OnSuccessListener<SparkUser>> newList = mRegisteredListeners.get(uid);
            if(newList != null && !newList.contains(listener)){
                newList.add(listener);
            }
            for(int i = 0; i < mSparkUsers.size(); ++i){
                SparkUser user = mSparkUsers.get(i);
                if(user.getUid() != null && user.getUid().equals(uid)){
                    listener.onSuccess(user);
                    break;
                }
            }
        }
    }

    public void removeUserListener(String uid, OnSuccessListener<SparkUser> listener){
        List<OnSuccessListener<SparkUser>> listeners = mRegisteredListeners.get(uid);
        if(listeners != null){
            listeners.remove(listener);
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if(R.id.menu_button == v.getId()){
            //HomeDrawerFragment drawer = new HomeDrawerFragment();
            //drawer.show(getSupportFragmentManager(), drawer.getClass().getSimpleName());
        }


        if(R.id.back_button == v.getId()){
            onBackPressed();
        }

        if(R.id.add_person_button == v.getId()){
            openFragment(SearchFragment.class.getCanonicalName(), false);
        }

    }

    /**
     * Called when a sliding pane's position changes.
     *
     * @param panel       The child view that was moved
     * @param slideOffset The new offset of this sliding pane within its range, from 0-1
     */
    @Override
    public void onPanelSlide(@NonNull View panel, float slideOffset) {
        binding.backButton.setVisibility(View.VISIBLE);
        binding.menuButton.setVisibility(View.VISIBLE);
        binding.menuButton.setAlpha(slideOffset);
        binding.backButton.setAlpha(1 - slideOffset);

        binding.toolbarContent.setVisibility(View.VISIBLE);
        binding.toolbarContainer.setVisibility(View.VISIBLE);
        binding.toolbarContainer.setAlpha(1 - slideOffset);
        binding.toolbarContent.setAlpha(slideOffset);

        int width = binding.toolbarContent.getWidth() / 5;
        binding.toolbarContainer.setTranslationX(slideOffset * width);
        binding.toolbarContent.setTranslationX((1 - slideOffset) * width);

        binding.floatingActionButton.setTranslationY((binding.floatingActionButton.getHeight() * 2) * (1 - slideOffset));
    }

    /**
     * Called when a sliding pane becomes slid completely open. The pane may or may not
     * be interactive at this point depending on how much of the pane is visible.
     *
     * @param panel The child view that was slid to an open position, revealing other panes
     */
    @Override
    public void onPanelOpened(@NonNull View panel) {
        binding.backButton.setVisibility(View.INVISIBLE);
        binding.menuButton.setVisibility(View.VISIBLE);
        binding.toolbarContent.setVisibility(View.VISIBLE);
        binding.toolbarContainer.setVisibility(View.INVISIBLE);

        binding.floatingActionButton.setTranslationY(0);

        AndroidUtils.closeKeyboard(this);
    }

    /**
     * Called when a sliding pane becomes slid completely closed. The pane is now guaranteed
     * to be interactive. It may now obscure other views in the layout.
     *
     * @param panel The child view that was slid to a closed position
     */
    @Override
    public void onPanelClosed(@NonNull View panel) {
        binding.backButton.setVisibility(View.VISIBLE);
        binding.menuButton.setVisibility(View.INVISIBLE);
        binding.toolbarContainer.setVisibility(View.VISIBLE);
        binding.toolbarContent.setVisibility(View.INVISIBLE);

        binding.floatingActionButton.setTranslationY(binding.floatingActionButton.getHeight() * 2);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        String uid = dataSnapshot.getKey();
        if(!mRegisteredReferences.containsKey(uid) || !mRegisteredListeners.containsKey(uid)){
            dataSnapshot.getRef().removeEventListener(this);
            return;
        }
        SparkUser user = SparkUser.fromDataSnapshot(dataSnapshot);
        if(user == null){
            onCancelled(DatabaseError.fromCode(DatabaseError.UNAVAILABLE));
            return;
        }
        for(int i = 0; i < mSparkUsers.size(); ++i){
            SparkUser user2 = mSparkUsers.get(i);
            if(user2.getUid() != null && user.getUid() != null && user.getUid().equals(user2.getUid())){
                mSparkUsers.remove(i);
                break;
            }
        }
        mSparkUsers.add(user);

        List<OnSuccessListener<SparkUser>> listeners = mRegisteredListeners.get(uid);
        if (listeners != null) {
            for (int i = 0, listenersSize = listeners.size(); i < listenersSize; i++) {
                OnSuccessListener<SparkUser> listener = listeners.get(i);
                listener.onSuccess(user);
            }
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
//        REMOVE
        Toast.makeText(this, databaseError.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed(){
        if(!binding.slidingPanelLayout.isOpen()){
            BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.container);
            if(fragment != null && fragment.onBackPressed()){
                return;
            }
        }

        if(!binding.slidingPanelLayout.isOpen()){
            binding.slidingPanelLayout.openPane();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if(mLastFragmentTag != null){
            outState.putString(KEY_LAST_FRAGMENT_TAG, mLastFragmentTag);
        }
        outState.putBoolean(KEY_LAST_PANEL_STATE, binding.slidingPanelLayout.isOpen());
    }
}
