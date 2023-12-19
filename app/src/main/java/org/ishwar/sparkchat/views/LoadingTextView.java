/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat.views;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class LoadingTextView extends AppCompatTextView {
    public static final long LOADING_ANIMATION_DURATION = 500L;

    public char progress_character;
    private int progress_steps;
    private int progress_step_index;

    private CharSequence givenText;

    private Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (progress_steps == 0 || givenText == null) {
                stopProgressForcibly();
                return;
            }

            if (progress_step_index-- <= 0) {
                progress_step_index = progress_steps;
            }

            setText(givenText.subSequence(0, givenText.length() - progress_step_index));
            handler.postDelayed(this, LoadingTextView.LOADING_ANIMATION_DURATION);
            
        }
    };

    public LoadingTextView(Context context) {
        super(context);
    }

    public LoadingTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void startProgress(CharSequence text) {
        setText(text);
        if (text.length() == 0) return;
        this.givenText = text;
        progress_character = givenText.charAt(givenText.length() - 1);
        this.progress_steps = 0;
        while (givenText.length() > progress_steps && givenText.length() - progress_steps > 0 && givenText.charAt(givenText.length() - 1 - progress_steps) == progress_character) {
            progress_steps++;
        }
        setText(givenText);
        progress_step_index = progress_step_index;
        handler.post(runnable);
    }

    public void stopProgressForcibly(){
        handler.removeCallbacks(runnable);
        setText(givenText);
        givenText = null;
        progress_step_index = 0;
        progress_steps = 0;
    }

    public int getProgressSteps(){
        return progress_steps;
    }
}
