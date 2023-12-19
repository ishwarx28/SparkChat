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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;

import org.ishwar.sparkchat.R;
import org.ishwar.sparkchat.databinding.ItemMainConnectionBinding;
import org.ishwar.sparkchat.firebase.FirebaseUtils;
import org.ishwar.sparkchat.fragments.BaseFragment;
import org.ishwar.sparkchat.models.SparkConnection;
import org.ishwar.sparkchat.models.SparkGroup;
import org.ishwar.sparkchat.models.SparkUser;
import org.ishwar.sparkchat.utils.TimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConnectionsFragment extends BaseFragment implements View.OnClickListener {

    private RecyclerView mConnectionsList;
    private MainConnectionAdapter mMainConnectionAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connections, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        this.mConnectionsList = view.findViewById(R.id.connections_list);
        mConnectionsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mMainConnectionAdapter = new MainConnectionAdapter();
        mConnectionsList.setAdapter(mMainConnectionAdapter);

        OnSuccessListener<SparkUser> onSuccess =  new OnSuccessListener<SparkUser>(){
            @Override
            public void onSuccess(SparkUser p1){
                getHomeActivity().removeUserListener(p1.getUid(), this);

                String message = getString(R.string.warning_first_message_to_oneself);

                SparkConnection conn = new SparkConnection(FirebaseUtils.getUid(), FirebaseUtils.getUid(), message, p1.getLastSeen() == null ? System.currentTimeMillis() : p1.getLastSeen(), -1);
                mMainConnectionAdapter.addItem(conn);
                conn = new SparkConnection("FdePqEUM7tZAkOq6Sv2z0jXs9U73", FirebaseUtils.getUid(), "Hello", System.currentTimeMillis() - 1000 * 60 * 60 * 24, 5);
                mMainConnectionAdapter.addItem(conn);
                mMainConnectionAdapter.notifyDataSetChanged();

                //mMainConnectionAdapter.notifyItemInserted(mMainConnectionAdapter.getItemCount() - 1);

                // fetch connections
                //mSenderQuery = FirebaseUtils.getConnectionsRef().orderByChild("sender").equalTo(FirebaseUtils.getUid());
                //mReceiverQuery = FirebaseUtils.getConnectionsRef().orderByChild("receiver").equalTo(FirebaseUtils.getUid());

                //mSenderQuery.addValueEventListener(ConnectionFragment.this);
                //mReceiverQuery.addValueEventListener(ConnectionFragment.this);

            }
        };
        getHomeActivity().listenToUser(FirebaseUtils.getUid(), onSuccess);
    }

    @Override
    public void onClick(View p1){
        int position = mConnectionsList.getChildAdapterPosition(p1);
        if(position >= 0 && mMainConnectionAdapter.getItemCount() > position){
            onItemClick(p1, position);
        }
    }

    private void onItemClick(View p1, int position){
        SparkConnection connection = mMainConnectionAdapter.getItem(position);

        if(mMainConnectionAdapter.getItemViewType(position) == HomeActivity.VIEW_TYPE_1){

        }

        if(connection == null) return;

        String uid = connection.getSender().equals(FirebaseUtils.getUid()) ? connection.getReceiver() : connection.getSender();
        getHomeActivity().openChatFragment(uid, false);
    }

    public class MainConnectionAdapter extends RecyclerView.Adapter{

        private List<SparkConnection> mSparkConnections = new ArrayList<>();
        private int mChatsCount;
        private int mGroupCounts;

        void addItem(SparkConnection chat){
            this.mSparkConnections.add(chat);
            mChatsCount++;
        }

        public void addItem(SparkGroup group){
            this.mSparkConnections.add(group);
            mGroupCounts++;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup p1, int p2){
            RecyclerView.ViewHolder holder;
            if(HomeActivity.VIEW_TYPE_1 == p2){
                holder = new HeaderViewHolder(getActivity().getLayoutInflater().inflate(R.layout.item_main_connection, p1, false));
                holder.itemView.setOnClickListener(ConnectionsFragment.this);
            }else if(HomeActivity.VIEW_TYPE_2 == p2){
                holder = new ItemViewHolder(ItemMainConnectionBinding.inflate(getActivity().getLayoutInflater(), p1, false));
                holder.itemView.setOnClickListener(ConnectionsFragment.this);
            }else if(HomeActivity.VIEW_TYPE_3 == p2){
                TextView msg = new TextView(getContext());
                msg.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                msg.setTextColor(ContextCompat.getColor(getContext(), R.color.secondaryTextColor));
                msg.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.secondary_text_size));
                msg.setTypeface(ResourcesCompat.getFont(getContext(), R.font.roboto_medium));
                int padding = (int) getContext().getResources().getDimension(R.dimen.medium_padding);
                msg.setPadding(padding, padding, padding, padding);
                msg.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                msg.setGravity(Gravity.CENTER);
                holder = new FooterViewHolder(msg);

            }else{
                throw new IllegalArgumentException();
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position){
            int viewType = getItemViewType(position);

            if(HomeActivity.VIEW_TYPE_3 == viewType){
                ((FooterViewHolder) holder).message.setText(String.format(getContext().getString(R.string.format_connection_counts), mChatsCount, mGroupCounts));
                return;
            }

            if(HomeActivity.VIEW_TYPE_1 == viewType){
                return;
            }

            if(HomeActivity.VIEW_TYPE_2 == viewType){
                SparkConnection connection = getItem(position);
                ItemViewHolder itemHolder = (ItemViewHolder) holder;

                String senderUid = connection.getSender().equals(FirebaseUtils.getUid()) ? connection.getReceiver() : connection.getSender();
                getHomeActivity().listenToUser(senderUid, itemHolder);

                boolean sentFromThisUser = connection.getSender().equals(FirebaseUtils.getUid());
                itemHolder.binding.messageReadIcon.setVisibility(sentFromThisUser ? View.VISIBLE : View.GONE);
                itemHolder.binding.unreadsBg.setVisibility(!sentFromThisUser && connection.getUnreads() > 0 ? View.VISIBLE : View.GONE);

                if(sentFromThisUser){
                    itemHolder.binding.messageReadIcon.setImageResource(connection.getUnreads() > 0 ? R.drawable.ic_message_unread : R.drawable.ic_message_read);
                }else if(connection.getUnreads() > 0 && 10 > connection.getUnreads()){
                    itemHolder.binding.unreads.setText(String.format(Locale.ENGLISH, "%d", connection.getUnreads()));
                }else if(connection.getUnreads() > 0){
                    itemHolder.binding.unreads.setText("9+");
                }

                itemHolder.binding.lastMessageTime.setText(TimeFormatter.format(connection.getSentTime()));
                itemHolder.binding.lastMessage.setText(connection.getLastMessage());
            }
        }

        @Override
        public int getItemViewType(int position){
            return position == 0 ? HomeActivity.VIEW_TYPE_1 : position == getItemCount() - 1 ? HomeActivity.VIEW_TYPE_3 : HomeActivity.VIEW_TYPE_2;
        }

        SparkConnection getItem(int position){
            return mSparkConnections.size() >= position && position > 0 ? mSparkConnections.get(position - 1) : null;
        }

        @Override
        public int getItemCount(){
            return mSparkConnections.size() + 2;
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements OnSuccessListener<SparkUser>{
        ItemMainConnectionBinding binding;
        ItemViewHolder(ItemMainConnectionBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onSuccess(SparkUser p1){
            binding.username.setText(p1.getName());
            binding.profileImage.setUserProfile(p1);
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder{
        HeaderViewHolder(View parent){
            super(parent);
            parent.findViewById(R.id.last_message_time).setVisibility(View.GONE);
            TextView title = parent.findViewById(R.id.username);
            TextView description = parent.findViewById(R.id.last_message);
            ImageView icon = parent.findViewById(R.id.profile_image);
            icon.setImageResource(R.drawable.logo_app_data);
            title.setText(getContext().getString(R.string.label_app_data));
            description.setText(getContext().getString(R.string.label_manage_data));
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder{
        TextView message;
        FooterViewHolder(TextView tv){
            super(tv);
            this.message = tv;
        }
    }

}
