/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat.utils;
import android.content.Intent;
import java.io.File;
import android.content.Context;

public class AppUtils{
    public static final String PATH_TERM_AND_PRIVACY_POLICY = "docs/terms_privacy_policy.html";
    public static final String PATH_COUNTRY_CODES = "data/country_codes.json";

    public static File getAutoCompleteModelPath(Context context){
        File path = new File(context.getCacheDir(), "autocomplete-models");
        try{ if (!path.exists()) path.mkdir(); }catch (Exception e){}
        return path;
    }

    public static void shareAppUrl(Context context){
        Intent feedback = new Intent(Intent.ACTION_SEND);
        feedback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        feedback.putExtra(Intent.EXTRA_TEXT, context.getString(org.ishwar.sparkchat.R.string.app_developer_gmail_address));
        feedback.setType("text/plain");
        context.startActivity(feedback);
    }
}