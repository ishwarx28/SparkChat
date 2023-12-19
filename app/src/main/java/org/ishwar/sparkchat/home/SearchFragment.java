package org.ishwar.sparkchat.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.ishwar.sparkchat.R;
import org.ishwar.sparkchat.databinding.FragmentSearchBinding;
import org.ishwar.sparkchat.databinding.ItemSearchToolbarBinding;
import org.ishwar.sparkchat.firebase.FirebaseUtils;
import org.ishwar.sparkchat.fragments.BaseFragment;
import org.ishwar.sparkchat.models.SparkUser;
import org.ishwar.sparkchat.utils.AutoCompletionProvider;
import org.ishwar.sparkchat.views.CircleImageView;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends BaseFragment implements OnClickListener, OnEditorActionListener, TextWatcher, ValueEventListener
{
	private static final String SHARED_PREF_SEARCH_HISTORY = "search-history";
	private static final int MAX_SEARCH_HISTORY = 10;

	private FragmentSearchBinding binding;
	private ItemSearchToolbarBinding toolbarBinding;

	private List<SparkUser> mSearchResults;
	private List<SparkUser> mSearchHistory;
	private SearchAdapter mSearchAdapter;

    private AutoCompletionProvider mCompletionProvider;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mCompletionProvider = new AutoCompletionProvider(getContext(), getTag(), 1);
		this.mSearchResults = new ArrayList<>();
		this.mSearchHistory = new ArrayList<>();
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        binding.searchList.setLayoutManager(new LinearLayoutManager(getContext()));
        this.mSearchAdapter = new SearchAdapter();
		binding.searchList.setAdapter(mSearchAdapter);

        getHomeActivity().getGlobalHandler().postDelayed(() -> {
            loadSearchHistory();
            showSearchHistory();

            ((ViewGroup) binding.getRoot()).removeView(binding.progressBar);
        }, HomeActivity.DEFAULT_FRAGMENT_LOADING_DELAY);
    }

    
    private void search(String query){
        query = query.trim().toLowerCase();
        if (query.length() <= 1) {
            mSearchAdapter.clear();
        }else{
            FirebaseUtils.getUserRef().orderByChild(SparkUser.KEY_USERNAME).startAt(query).endAt(query.concat("\uf8ff")).addListenerForSingleValueEvent(SearchFragment.this);
        }
    }
	
	private void loadSearchHistory(){
		try {
		    String json = getHomeActivity().getLocalStore().getString(SHARED_PREF_SEARCH_HISTORY, null);
            JSONArray array = new JSONArray(json);

            mSearchHistory.clear();
            for(int i = 0; i < array.length(); ++i){
                SparkUser user = SparkUser.fromJson(array.getString(i));
                if (user != null) {
                    mSearchHistory.add(user);
                }
            }
        } catch (Exception e) {
		    e.printStackTrace();
        }
	}
	
	private void showSearchHistory(){
		mSearchAdapter.updateData(mSearchHistory);
	}
	
	private void saveToSearchHistory(SparkUser user){
		int index = -1;
		for(int i = 0; i < mSearchHistory.size(); ++i){
			if(mSearchHistory.get(i).getUid() != null && mSearchHistory.get(i).getUid().equals(user.getUid())){
				index = i;
				break;
			}
		}

		if (index == 0) {
            return;
        }

		if(index > -1){
			mSearchHistory.remove(index);
		}
		
		mSearchHistory.add(0, user);

        JSONArray array = new JSONArray();
		for(index = 0; index < Math.min(MAX_SEARCH_HISTORY, mSearchHistory.size()); ++index){
			array.put(mSearchHistory.get(index).toJson());
		}

        getHomeActivity().getLocalStore().edit().putString(SHARED_PREF_SEARCH_HISTORY, array.toString()).apply();
	}

    @Override
    public void updateToolbar(){
        ViewGroup parent = getHomeActivity().getToolbarContainer();
        parent.removeAllViews();
        toolbarBinding = ItemSearchToolbarBinding.inflate(getLayoutInflater(), parent, true);
        this.toolbarBinding.edSearch.setAutoCompletionProvider(mCompletionProvider);
        this.toolbarBinding.edSearch.setOnEditorActionListener(this);
        this.toolbarBinding.edSearch.addTextChangedListener(this);
       this.toolbarBinding.clearButton.setOnClickListener(this);
    }

    @Override
    public void onDataChange(@NotNull DataSnapshot p1){
        if(p1.exists() && p1.hasChildren()){
			List<SparkUser> results = new ArrayList<>();
			for(DataSnapshot snapshot : p1.getChildren()){
				SparkUser user = snapshot.getValue(SparkUser.class);
				if(user == null) continue;
				user.setUid(snapshot.getKey());
				results.add(user);
			}
			mSearchAdapter.updateData(results);

		}else{
			mSearchAdapter.clear();
		}
    }

    @Override
    public void onCancelled(@NonNull DatabaseError p1){
        mSearchAdapter.clear();
//        REMOVE
		android.widget.Toast.makeText(getContext(), p1 + "", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onEditorAction(TextView p1, int p2, KeyEvent p3){
        String query = toolbarBinding.edSearch.getOnlyText().trim();
        if(query.isEmpty()) return true;
        mCompletionProvider.update(query);
        search(query);
        toolbarBinding.edSearch.clearFocus();
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4){
        //        required
    }

    @Override
    public void onTextChanged(CharSequence p1, int p2, int p3, int p4){
        //      required
    }

    @Override
    public void afterTextChanged(Editable p1){
        String query = toolbarBinding.edSearch.getOnlyText();
        if(query.trim().isEmpty()){
            toolbarBinding.clearButton.setVisibility(View.GONE);
        }else{
            toolbarBinding.clearButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View p1){
        if(p1.getId() == R.id.clear_button){
            toolbarBinding.edSearch.setText("");
            showSearchHistory();
        }
		
		if(p1.getId() == R.id.item_main){
			int position = binding.searchList.getChildAdapterPosition(p1);
			SparkUser result = mSearchResults.get(position);
			saveToSearchHistory(result);
			getHomeActivity().openChatFragment(result.getUid(), false);
		}
    }
    
    private static class ResultDiffUtil extends DiffUtil.Callback
    {
		private List<SparkUser> oldData;
		private List<SparkUser> newData;
		
		ResultDiffUtil(List<SparkUser> oldData, List<SparkUser> newData){
			this.oldData = oldData;
			this.newData = newData;
		}
		
		@Override
		public int getOldListSize() {
		    return oldData.size();
		}

		@Override
		public int getNewListSize() {
		    return newData.size();
		}

		@Override
		public boolean areItemsTheSame(int arg0, int arg1) {
		    return oldData.get(arg0).getUid().equals(newData.get(arg1).getUid());
		}

		@Override
		public boolean areContentsTheSame(int arg0, int arg1) {
		    SparkUser a = oldData.get(arg0);
			SparkUser b = newData.get(arg1);
			boolean sameUsername = (a.getUsername() != null && a.getUsername().equals(b.getUsername())) || (b.getUsername() != null && b.getUsername().equals(a.getUsername()));
			boolean sameDp = (a.getPhotoUrl() != null && a.getPhotoUrl().equals(b.getPhotoUrl())) && (b.getPhotoUrl() != null && b.getPhotoUrl().equals(a.getPhotoUrl()));
			return sameDp && sameUsername;
		}
		
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder>
    {
		void clear(){
			mSearchResults.clear();
			this.notifyDataSetChanged();
		}
		
		void updateData(List<SparkUser> data){
			final ResultDiffUtil diffUtil = new ResultDiffUtil(mSearchResults, data);
			final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffUtil);
			
			mSearchResults.clear();
			mSearchResults.addAll(data);
			diffResult.dispatchUpdatesTo(this);
		}

        @NotNull
        @Override
        public SearchFragment.SearchViewHolder onCreateViewHolder(@NotNull ViewGroup p1, int p2){
            SearchViewHolder holder = new SearchViewHolder(getLayoutInflater().inflate(R.layout.item_main_connection, p1, false));
            holder.itemView.setOnClickListener(SearchFragment.this);
			return holder;
        }

        @Override
        public void onBindViewHolder(@NotNull SearchFragment.SearchViewHolder p1, int p2){
            SparkUser result = mSearchResults.get(p2);
            p1.username.setText(result.getUsername());
            p1.name.setText(result.getName());
            p1.profileImage.setUserProfile(result);
        }

        @Override
        public int getItemCount(){
            return mSearchResults == null ? 0 : mSearchResults.size();
        }
    }
    
    private static class SearchViewHolder extends RecyclerView.ViewHolder{
        
        CircleImageView profileImage;
        public TextView name;
        TextView username;
        
        SearchViewHolder(View parent){
            super(parent);
            parent.findViewById(R.id.last_message_time).setVisibility(View.GONE);
            name = parent.findViewById(R.id.username);
            username = parent.findViewById(R.id.last_message);
            profileImage = parent.findViewById(R.id.profile_image);
        }
    }
    
}
