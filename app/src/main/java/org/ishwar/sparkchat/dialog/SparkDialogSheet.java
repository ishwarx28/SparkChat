/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat.dialog;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.ishwar.sparkchat.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class SparkDialogSheet extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private static WeakReference<SparkDialogSheet> mLastInstance = null;

    public static void destroy(){
        if(mLastInstance != null && mLastInstance.get() != null && mLastInstance.get().isShowing()){
            mLastInstance.get().dismiss();
            mLastInstance = null;
        }
    }

    public static SparkDialogSheet make(Context context){
        destroy();
        SparkDialogSheet instance = new SparkDialogSheet(context);
        mLastInstance = new WeakReference<>(instance);
        return instance;
    }

    private Context mContext;

    // Dimension values
    private final int primaryPadding;
    private final int mediumPadding;
    private final int secondaryPadding;

    private final BottomSheetDialog sheet;
    private final RecyclerView itemsList;
    private final ArrayList<View> items = new ArrayList<>();

    private SparkDialogSheet(Context context) {
        this.mContext = context;

        this.sheet = new BottomSheetDialog(context);

        // Initialize dimension values
        primaryPadding = (int) context.getResources().getDimension(R.dimen.primary_padding);
        mediumPadding = (int) context.getResources().getDimension(R.dimen.medium_padding);
        secondaryPadding = (int) context.getResources().getDimension(R.dimen.secondary_padding);

        LinearLayout content = new LinearLayout(mContext);
        content.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        content.setPadding(0, primaryPadding, 0, 0);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);

        View hook = new View(mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(secondaryPadding, primaryPadding);
        lp.setMargins(mediumPadding, mediumPadding, mediumPadding, mediumPadding);
        hook.setLayoutParams(lp);
        hook.setBackgroundResource(R.drawable.bg_round);

        itemsList = new RecyclerView(mContext);
        itemsList.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        itemsList.setLayoutManager(new LinearLayoutManager(context));

        content.addView(hook);
        content.addView(itemsList);
        sheet.setContentView(content);
    }

    public void setCancelable(boolean flag) {
        sheet.setCancelable(flag);
    }

    public void show() {
        itemsList.setAdapter(this);
        notifyDataSetChanged();
        sheet.show();
    }

    public boolean isShowing() {
        return sheet.isShowing();
    }

    public void dismiss() {
        sheet.dismiss();
    }

    @Override
    public void onClick(View view) {
        dismiss();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public View getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public <T extends View> T findById(int id) {
        return sheet.findViewById(id);
    }

    public SparkDialogSheet addMessage(CharSequence message) {
        TextView msg = new TextView(mContext);
        msg.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        msg.setTextColor(ContextCompat.getColor(mContext, R.color.secondaryTextColor));
        msg.setMovementMethod(LinkMovementMethod.getInstance());
        msg.setClickable(true);
        msg.setLinkTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        msg.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.secondary_text_size));
        msg.setText(message);
        msg.setTypeface(ResourcesCompat.getFont(mContext, R.font.roboto_medium));
        msg.setPadding(secondaryPadding, primaryPadding, secondaryPadding, mediumPadding);
        msg.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        msg.setGravity(Gravity.CENTER);
        items.add(msg);

        if (sheet.isShowing()) {
            notifyItemInserted(items.size() - 1);
        }
        return this;
    }

    public SparkDialogSheet addDivider() {
        View divider = new View(mContext);
        divider.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.height_divider)));
        divider.setBackgroundColor(ContextCompat.getColor(mContext, R.color.divider_color));
        items.add(divider);
        if (sheet.isShowing()) {
            notifyItemInserted(items.size() - 1);
        }

        return this;
    }

    public SparkDialogSheet addButton(int id, CharSequence label, View.OnClickListener listener) {
        return addButton(id, label, ContextCompat.getColor(mContext, R.color.colorPrimary), listener);
    }

    public SparkDialogSheet addButton(int id, CharSequence label, int textColor, View.OnClickListener listener) {
        TextView button = new TextView(mContext);
        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setTextColor(textColor);
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.primary_bttn_text_size));
        button.setId(id);
        button.setText(label);
        button.setTypeface(ResourcesCompat.getFont(mContext, R.font.roboto_medium));
        button.setPadding(secondaryPadding, mediumPadding, secondaryPadding, mediumPadding);
        button.setGravity(Gravity.CENTER);
        button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        button.setBackgroundResource(R.drawable.bg_ripple);
        button.setOnClickListener(listener == null ? this : listener);
        button.setSingleLine();
        items.add(button);
        if (sheet.isShowing()) {
            notifyItemInserted(items.size() - 1);
        }

        return this;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout root = new LinearLayout(mContext);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.setOrientation(LinearLayout.HORIZONTAL);
        return new RecyclerView.ViewHolder(root){};
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewGroup parent = (ViewGroup) holder.itemView;
        parent.removeAllViews();
        parent.addView(getItem(position));
    }
}
