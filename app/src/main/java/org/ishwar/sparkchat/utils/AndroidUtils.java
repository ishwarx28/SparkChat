/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.ishwar.sparkchat.home.HomeActivity;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class AndroidUtils
{
    public static void endApplication(){
        android.os.Process.killProcess(android.os.Process.myPid());
        Runtime.getRuntime().exit(1);
    }

    public static void restartApp(Context context) {
        context = context.getApplicationContext();
        try {
            PackageManager pm = context.getPackageManager();
            ComponentName component = pm.getLaunchIntentForPackage(context.getPackageName()).getComponent();
            Intent intent = Intent.makeRestartActivityTask(component);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        endApplication();
    }

    public static void composeGmail(Context context, String sendTo, String subject, String message){
        Intent feedback = new Intent(Intent.ACTION_SENDTO);
        feedback.setData(Uri.parse("mailto:"));
        feedback.putExtra(Intent.EXTRA_EMAIL, new String[]{sendTo});
        feedback.putExtra(Intent.EXTRA_SUBJECT, subject);
        feedback.putExtra(Intent.EXTRA_TEXT, message);
        feedback.setPackage("com.google.android.gm");
        try{
            context.startActivity(Intent.createChooser(feedback, "Send Crash Report To SparkChat").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }catch(Exception e){
            feedback = new Intent(Intent.ACTION_SEND);
            feedback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            feedback.putExtra(Intent.EXTRA_TEXT, e.getMessage());
            feedback.setType("text/plain");
            context.startActivity(feedback);
        }
    }

    public static String readAsset(Context context, String path){
        try {
            InputStream stream = context.getAssets().open(path);
            byte[] buffer = new byte[stream.available()];
            stream.read(buffer);
            stream.close();
            return new String(buffer, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeKeyboard(Activity activity) {
        View currentFocusedView = activity.getCurrentFocus();
        if(currentFocusedView != null){
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), 0);
            currentFocusedView.clearFocus();
        }else{
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }
}