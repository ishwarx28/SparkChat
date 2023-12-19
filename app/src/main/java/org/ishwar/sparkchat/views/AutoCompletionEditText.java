package org.ishwar.sparkchat.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import androidx.core.content.ContextCompat;

import org.ishwar.sparkchat.utils.AutoCompletionProvider;
import org.ishwar.sparkchat.utils.CompletionTokenizer;

@SuppressLint("AppCompatCustomView")
public class AutoCompletionEditText extends EditText implements OnEditorActionListener
{
    private boolean mCompletionTaskCompleted;
    private boolean mIsCompletionAdded;
    private int mAddedCompletionStart;
    private int mCompletionTextColor;

    private AutoCompletionProvider mCompletionProvider;
    
    private OnEditorActionListener mUserOnEditorActionListener;

    public AutoCompletionEditText(Context context){
        super(context);
        init(context);
    }

    public AutoCompletionEditText(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context);
    }
    
    public AutoCompletionEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        mCompletionTaskCompleted = true;
        mIsCompletionAdded = false;
        this.mCompletionTextColor = ContextCompat.getColor(context, android.R.color.darker_gray);
        super.setOnEditorActionListener(this);
        
        setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        setImeActionLabel("DONE", EditorInfo.IME_ACTION_DONE);
        setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    public int getCompletionTextColor(){
        return mCompletionTextColor;
    }

    public void setCompletionTextColor(int color){
        this.mCompletionTextColor = color;
    }

    @Override
    public void setOnEditorActionListener(TextView.OnEditorActionListener l){
        this.mUserOnEditorActionListener = l;
    }

    public AutoCompletionProvider getAutoCompletionProvider(){
        return mCompletionProvider;
    }

    public void setAutoCompletionProvider(AutoCompletionProvider provider){
        this.mCompletionProvider = provider;
    }
    
    public String getOnlyText(){
        Editable text = getText();
        StringBuilder builder = new StringBuilder(text);
        
        CompletionSpan[] allSpans = text.getSpans(0, text.length(), CompletionSpan.class);
        CompletionSpan span = allSpans != null && allSpans.length > 0 ? allSpans[0] : null;

        if(span == null) return builder.toString();

        int start = text.getSpanStart(span);
        int end = text.getSpanEnd(span);
        return builder.delete(start, end).toString();
    }

    public void adaptCompletion(){
        if(!mIsCompletionAdded || mCompletionProvider == null) return; else mIsCompletionAdded = false;

        Editable text = getText();
        CompletionSpan[] allSpans = text.getSpans(0, text.length(), CompletionSpan.class);
        CompletionSpan span = allSpans != null && allSpans.length > 0 ? allSpans[0] : null;

        if(span == null) return;

        mAddedCompletionStart = text.getSpanEnd(span);
        text.removeSpan(span);
        text.removeSpan(span);
        setSelection(mAddedCompletionStart);
        pingCompletion();
    }

    public void clearCompletionSpan(){
        Editable text = getText();
        if(!mIsCompletionAdded) return; else mIsCompletionAdded = false;

        CompletionSpan[] allSpans = text.getSpans(0, text.length(), CompletionSpan.class);
        CompletionSpan span = allSpans != null && allSpans.length > 0 ? allSpans[0] : null;

        if(span == null) return;

        int start = text.getSpanStart(span);
        int end = text.getSpanEnd(span);
        text.removeSpan(span);
        text.removeSpan(span);
        text.delete(start, end);
    }
    
    public void pingCompletion(){
        if(!mCompletionTaskCompleted || mCompletionProvider == null){
            return;
        }
        int cursor = getSelectionStart();

        mCompletionTaskCompleted = false; 

        Editable editableText = getText();
        clearCompletionSpan();

        if(cursor > 0){


            int start = cursor;
            for(int j = 0; j < mCompletionProvider.getWindowSize() + 1 && start > 0; ++j){
                start = CompletionTokenizer.findTokenStart(editableText, start);
            }

            int end = CompletionTokenizer.findTokenEnd(editableText, cursor);

            start = Math.max(0, start);
            end = Math.max(start, end);

            String currentToken = editableText.subSequence(start, end).toString();
            String nextToken = mCompletionProvider.nextToken(currentToken);

            if(nextToken != null && !nextToken.isEmpty()){
                SpannableString spannedToken = new SpannableString(nextToken);
                spannedToken.setSpan(new CompletionSpan(mCompletionTextColor), 0, spannedToken.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

                editableText.insert(end, spannedToken);
                mAddedCompletionStart = end;
                setSelection(mAddedCompletionStart);
                mIsCompletionAdded = true;
            }
        }


        mCompletionTaskCompleted = true;
    }
    
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if((event == null || actionId == EditorInfo.IME_NULL) && (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_NEXT)){

            if(mIsCompletionAdded){
                if(event == null || event.getAction() == KeyEvent.ACTION_DOWN){
                    adaptCompletion();
                }

                return true;
            }

            if(!mIsCompletionAdded && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isSingleLine()){
                if(event == null || event.getAction() == KeyEvent.ACTION_DOWN){
                    Editable text = getText();
                    text.replace(getSelectionStart(), getSelectionEnd(), "\n");
                }

                return true;
            }
        }
        
        return mUserOnEditorActionListener != null && mUserOnEditorActionListener.onEditorAction(v, actionId, event);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd){
        super.onSelectionChanged(selStart, selEnd);

        if(mIsCompletionAdded && mCompletionTaskCompleted && (mAddedCompletionStart != selStart || mAddedCompletionStart != selEnd)){
            clearCompletionSpan();
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter){
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        
        if(lengthBefore > lengthAfter || lengthAfter == lengthBefore){
            return;
        }
        pingCompletion();
        
    }
    
    public static class CompletionSpan extends ForegroundColorSpan{
        CompletionSpan(int color){
            super(color);
        }
    }
}
