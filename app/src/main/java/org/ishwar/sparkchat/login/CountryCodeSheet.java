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
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.ishwar.sparkchat.R;
import org.ishwar.sparkchat.utils.AndroidUtils;
import org.ishwar.sparkchat.utils.AppUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CountryCodeSheet extends RecyclerView.Adapter implements TextWatcher, View.OnClickListener{

    private Context mContext;
    private BottomSheetDialog sheet;

    private final List<Pair<String, String>> mCountryCodes = new ArrayList<>();

    private EditText ed_search;
    private RecyclerView country_list;
    private TextView tv_country_code;

    public CountryCodeSheet(Context context, TextView tv_country_code) {
        this.mContext = context;
        this.tv_country_code = tv_country_code;
        try {
            JSONArray array = new JSONArray(AndroidUtils.readAsset(mContext, AppUtils.PATH_COUNTRY_CODES));
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                mCountryCodes.add(new Pair<>(object.getString("name"), object.getString("dial_code")));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int primaryPadding = (int) mContext.getResources().getDimension(R.dimen.primary_padding);
        int mediumPadding = (int) mContext.getResources().getDimension(R.dimen.medium_padding);

        LinearLayout content = new LinearLayout(mContext);
        content.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        content.setPadding(0, primaryPadding, 0, 0);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);

        ed_search = new EditText(mContext);
        LinearLayout.MarginLayoutParams layoutParams = new LinearLayout.MarginLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(mediumPadding, primaryPadding, mediumPadding, mediumPadding);
        ed_search.setLayoutParams(layoutParams);
        ed_search.setPadding(mediumPadding, mediumPadding, mediumPadding, primaryPadding);
        ed_search.setBackgroundResource(R.drawable.bg_round);
        ed_search.setHint(R.string.label_search);
        ed_search.setSingleLine(true);
        ed_search.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.primary_bttn_text_size));
        ed_search.setTextColor(ContextCompat.getColor(mContext, R.color.primaryTextColor));
        ed_search.setHintTextColor(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
        Drawable dr = AppCompatResources.getDrawable(mContext, R.drawable.ic_search);
        if (dr != null) {
            dr.setTint(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
        }
        ed_search.setCompoundDrawablesWithIntrinsicBounds(dr, null, null, null);
        ed_search.setCompoundDrawablePadding(mediumPadding);
        ed_search.setTypeface(ResourcesCompat.getFont(mContext, R.font.roboto_medium));
        ed_search.setGravity(Gravity.CENTER_VERTICAL);
        ed_search.addTextChangedListener(this);

        country_list = new RecyclerView(mContext);
        country_list.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        country_list.setLayoutManager(new LinearLayoutManager(mContext));
        country_list.setAdapter(this);

        content.addView(ed_search);
        content.addView(country_list);

        this.sheet = new BottomSheetDialog(mContext, R.style.SparkChat_BottomSheetDialogStyle);
        sheet.setContentView(content);
    }

    @Override
    public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4){}
    @Override
    public void onTextChanged(CharSequence p1, int p2, int p3, int p4){}

    @Override
    public void afterTextChanged(@NonNull Editable p1){
        final String search_term = p1.toString().trim().toLowerCase();
        Collections.sort(mCountryCodes, (p11, p2) -> {
            if (search_term.isEmpty()) {
                return p11.first.compareToIgnoreCase(p2.first);
            } else {
                int s1 = Integer.compare(CountryCodeSheet.this.similarity(search_term, p2.first.toLowerCase()), CountryCodeSheet.this.similarity(search_term, p11.first.toLowerCase()));
                int s2 = Integer.compare(CountryCodeSheet.this.similarity(search_term, p11.second.toLowerCase()), CountryCodeSheet.this.similarity(search_term, p2.second.toLowerCase()));
                return Integer.compare(s1, s2);
            }
        });
        country_list.getAdapter().notifyDataSetChanged();
        country_list.scrollToPosition(0);
    }

    @Override
    public void onClick(View p1){

        if(p1 == tv_country_code){
            sheet.show();
            ed_search.setText(tv_country_code.getText());
            ed_search.setSelection(ed_search.getText().length());
        }else if(p1 instanceof LinearLayout){
            LinearLayout parent = (LinearLayout) p1;
            String countryCode = ((TextView) parent.getChildAt(1)).getText().toString().trim();
            tv_country_code.setText(countryCode);
            sheet.dismiss();
        }
    }

    private int similarity(String w1, String w2){
        return w2.startsWith(w1) ? 50 : w2.contains(w1) ? Math.min(49, w2.length() - w2.indexOf(w1)) : 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int mediumPadding = (int) mContext.getResources().getDimension(R.dimen.medium_padding);
        int secondaryPadding = (int) mContext.getResources().getDimension(R.dimen.secondary_padding);

        LinearLayout content = new LinearLayout(mContext);
        content.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        content.setPadding(secondaryPadding, mediumPadding, secondaryPadding, mediumPadding);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER);
        content.setBackgroundResource(R.drawable.bg_ripple);

        TextView country_name = new TextView(mContext);
        country_name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        country_name.setTextColor(ContextCompat.getColor(mContext, R.color.primaryTextColor));
        country_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.medium_text_size));
        country_name.setTypeface(ResourcesCompat.getFont(mContext, R.font.roboto_medium));
        country_name.setPadding(0, 0, mediumPadding, 0);
        country_name.setSingleLine(true);
        country_name.setEllipsize(TextUtils.TruncateAt.END);

        TextView country_code = new TextView(mContext);
        country_code.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        country_code.setTextColor(ContextCompat.getColor(mContext, R.color.primaryTextColor));
        country_code.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.secondary_text_size));
        country_code.setTypeface(ResourcesCompat.getFont(mContext, R.font.roboto_medium));
        country_name.setSingleLine(true);

        content.addView(country_name);
        content.addView(country_code);
        content.setOnClickListener(CountryCodeSheet.this);

        return new RecyclerView.ViewHolder(content){};
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder p1, int p2){
        ViewGroup parent = (ViewGroup) p1.itemView;
        ((TextView) parent.getChildAt(0)).setText(mCountryCodes.get(p2).first);
        ((TextView) parent.getChildAt(1)).setText(mCountryCodes.get(p2).second);
    }

    @Override
    public int getItemCount(){
        return mCountryCodes.size();
    }

}