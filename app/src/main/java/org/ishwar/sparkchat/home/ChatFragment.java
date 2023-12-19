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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.ishwar.sparkchat.R;
import org.ishwar.sparkchat.databinding.FragmentChatBinding;
import org.ishwar.sparkchat.databinding.ItemChatMessageEndBinding;
import org.ishwar.sparkchat.databinding.ItemChatMessageStartBinding;
import org.ishwar.sparkchat.databinding.ItemChatToolbarBinding;
import org.ishwar.sparkchat.databinding.ItemMessageReportBinding;
import org.ishwar.sparkchat.firebase.FirebaseUtils;
import org.ishwar.sparkchat.fragments.BaseFragment;
import org.ishwar.sparkchat.models.SparkMessage;
import org.ishwar.sparkchat.models.SparkUser;
import org.ishwar.sparkchat.utils.AutoCompletionProvider;
import org.ishwar.sparkchat.utils.TimeFormatter;

import java.util.ArrayList;

public class ChatFragment extends BaseFragment implements View.OnClickListener, ChildEventListener {
    private static final String TAG = "ChatFragment";
    private static final String SHARED_PREF_DRAFT_MESSAGE = "draft-msg";
    private static final int DEFAULT_MAX_MESSAGE_LOADING_NUMBER = 25;
    private static final Long DEFAULT_MESSAGE_GROUPING_DURATION = 60000L;

    private FragmentChatBinding binding;
    private ItemChatToolbarBinding toolbarBinding;

    private MessageListAdapter mMessageAdapter;

    private DatabaseReference mReceiverUserRef;
    private DatabaseReference mChatsRef;
    private Query mChatsQuery;

    private SparkUser mReceiverUser;
    private ArrayList<String> mChatKeys;
    private ArrayList<SparkMessage> mChatMessages;

    private ValueEventListener mReceiverUserListener = new ValueEventListener() {
        @SuppressLint("RestrictedApi")
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            mReceiverUser = SparkUser.fromDataSnapshot(dataSnapshot);
            if (mReceiverUser == null) {
                onCancelled(DatabaseError.fromCode(DatabaseError.UNAVAILABLE));
                return;
            }

            updateToolbarInfo();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Toast.makeText(getContext(), databaseError.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.e(TAG, "onCreateView");

        this.mChatKeys = new ArrayList<>();
        this.mChatMessages = new ArrayList<>();

        String mReceiverUid = getTag();
        this.mReceiverUserRef = FirebaseUtils.getUserRef().child(mReceiverUid);

        this.mChatsRef = FirebaseUtils.getChatsRef().child(FirebaseUtils.combinedKey(mReceiverUid, FirebaseUtils.getUid()));
        this.mChatsQuery = mChatsRef.limitToLast(DEFAULT_MAX_MESSAGE_LOADING_NUMBER);

        this.binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View parent, Bundle savedInstanceState) {
        super.onViewCreated(parent, savedInstanceState);
        Log.e(TAG, "onViewCreated");

        binding.edInputBox.setAutoCompletionProvider(new AutoCompletionProvider(getContext(), getTag()));
        binding.edInputBox.setText(getHomeActivity().getLocalStore().getString(SHARED_PREF_DRAFT_MESSAGE + getTag(), ""));

        binding.sendButton.setOnClickListener(this);
        binding.attachmentButton.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        binding.chatList.setLayoutManager(layoutManager);

        this.mMessageAdapter = new MessageListAdapter();
        binding.chatList.setAdapter(mMessageAdapter);

        getHomeActivity().getGlobalHandler().postDelayed(() -> {
            mChatsQuery.addChildEventListener(this);
            Log.e(TAG, "Child Event Listener attached to reference: " + mChatsQuery.getPath());
        },HomeActivity.DEFAULT_FRAGMENT_LOADING_DELAY);
    }

    private void sendMessage(String message) {
        SparkMessage sparkMessage = new SparkMessage(FirebaseUtils.getUid(), getTag(), message, System.currentTimeMillis());
        DatabaseReference push = mChatsRef.push();
        push.setValue(sparkMessage);
    }

    @Override
    public void onClick(@NonNull View p1) {

        if (R.id.send_button == p1.getId()) {
            String text = binding.edInputBox.getOnlyText().trim();
            if (text.length() > 0) {
                binding.edInputBox.getAutoCompletionProvider().update(text);
                binding.edInputBox.setText("");

                sendMessage(text);
            }
        }

        if (R.id.attachment_button == p1.getId()) {
            binding.edInputBox.getAutoCompletionProvider().deleteModel();
        }
    }

    @Override
    public void updateToolbar() {
        ViewGroup parent = getHomeActivity().getToolbarContainer();
        parent.removeAllViews();
        toolbarBinding = ItemChatToolbarBinding.inflate(getLayoutInflater(), parent, true);

        if (mReceiverUser != null) {
            updateToolbarInfo();
        }
    }

    private void updateToolbarInfo() {
        toolbarBinding.toolbarIcon.setUserProfile(mReceiverUser);
        toolbarBinding.toolbarTitle.setText(mReceiverUser.getName());
        if ((mReceiverUser.getLastSeen() == null || mReceiverUser.getLastSeen() == 0) && mReceiverUser.getUsername() != null) {
            toolbarBinding.toolbarSubtitle.setText('@' + mReceiverUser.getUsername());
        } else if (mReceiverUser.getLastSeen() == null || mReceiverUser.getLastSeen() == 0) {
            toolbarBinding.toolbarSubtitle.setText(R.string.status_user_deleted);
        } else {
            toolbarBinding.toolbarSubtitle.setText(TimeFormatter.format(mReceiverUser.getLastSeen()));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");

        if (mReceiverUserRef != null) {
            mReceiverUserRef.addListenerForSingleValueEvent(mReceiverUserListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");

        String message = binding.edInputBox.getOnlyText().trim();
        getHomeActivity().getLocalStore().edit().putString(SHARED_PREF_DRAFT_MESSAGE + getTag(), message).apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG, "onDestroyView");

        if (mChatsQuery != null) {
            mChatsQuery.removeEventListener(this);
            Log.e(TAG, "Child Event Listener detached from reference: " + mChatsQuery.getPath());
        }
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        String key = dataSnapshot.getKey();

        Log.e(TAG, "onChildAdded(" + dataSnapshot + ", " + key);

        SparkMessage sparkMessage = dataSnapshot.getValue(SparkMessage.class);
        if (sparkMessage == null) {
            return;
        }

        mChatKeys.add(key);
        mChatMessages.add(sparkMessage);
        mMessageAdapter.notifyItemInserted(mChatMessages.size() - 1);

        if (binding.progressBar.getVisibility() == View.VISIBLE) {
            binding.progressBar.setVisibility(View.GONE);
            ((ViewGroup) binding.getRoot()).removeView(binding.progressBar);
        }
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder;

            if (viewType == HomeActivity.VIEW_TYPE_1) {
                holder = new MessageViewStartHolder(ItemChatMessageStartBinding.inflate(getLayoutInflater(), parent, false));
            } else if (viewType == HomeActivity.VIEW_TYPE_2) {
                holder = new MessageViewEndHolder(ItemChatMessageEndBinding.inflate(getLayoutInflater(), parent, false));
            } else {
                holder = new SeenViewHolder(ItemMessageReportBinding.inflate(getLayoutInflater(), parent, false));
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            SparkMessage message = getItem(position);

            if (holder instanceof MessageViewStartHolder) {
                ItemChatMessageStartBinding chatBinding = ((MessageViewStartHolder) holder).binding;
                getHomeActivity().listenToUser(message.getSender(), sparkUser -> {
                    chatBinding.username.setText(sparkUser.getName());
                    chatBinding.profileImage.setUserProfile(sparkUser);
                });
                chatBinding.message.setText(message.getMessage());
                chatBinding.time.setText(TimeFormatter.format(message.getTimestamp()));
            } else if (holder instanceof MessageViewEndHolder) {
                ItemChatMessageEndBinding chatBinding = ((MessageViewEndHolder) holder).binding;
                chatBinding.message.setText(message.getMessage());
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return HomeActivity.VIEW_TYPE_3;
            }

            if (position == 0) {
                return HomeActivity.VIEW_TYPE_1;
            }

            SparkMessage lastMessage = getItem(position - 1);
            SparkMessage currentMessage = getItem(position);

            if (DEFAULT_MESSAGE_GROUPING_DURATION >= currentMessage.getTimestamp() - lastMessage.getTimestamp() && lastMessage.getSender().equals(currentMessage.getSender())) {
                return HomeActivity.VIEW_TYPE_2;
            }

            return HomeActivity.VIEW_TYPE_1;
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return mChatKeys.size() + 1;
        }

        public SparkMessage getItem(int position) {
            if (position >= 0 && mChatMessages.size() > position) {
                return mChatMessages.get(position);
            }
            return null;
        }
    }

    private static class MessageViewStartHolder extends RecyclerView.ViewHolder {
        ItemChatMessageStartBinding binding;
        MessageViewStartHolder(@NonNull ItemChatMessageStartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static class MessageViewEndHolder extends RecyclerView.ViewHolder {
        ItemChatMessageEndBinding binding;
        MessageViewEndHolder(@NonNull ItemChatMessageEndBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static class SeenViewHolder extends RecyclerView.ViewHolder {
        ItemMessageReportBinding binding;
        SeenViewHolder(@NonNull ItemMessageReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
