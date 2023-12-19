/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat;

import android.app.Application;
import android.os.Build;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import org.ishwar.sparkchat.utils.AndroidUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class SparkApplication extends Application {

    private Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        setupUncaughtExceptionHandler();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseApp.initializeApp(this);
    }

    private void setupUncaughtExceptionHandler(){
        this.defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.UncaughtExceptionHandler handler = (p1, p2) -> {
            StringWriter sw = new StringWriter();
            p2.printStackTrace(new PrintWriter(sw));

            String title = "Unhandled exception occurred";

            StringBuilder errorText = new StringBuilder(title).append(" on ").append(new Date().toString()).append("\n\n");
            errorText.append("Device Info:\n");
            errorText.append("Model: ").append(Build.MODEL).append("\n");
            errorText.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n");
            errorText.append("SDK Version: ").append(Build.VERSION.SDK_INT).append("\n\n");
            errorText.append("Stack Trace:\n");
            errorText.append(sw.toString());

            AndroidUtils.composeGmail(SparkApplication.this, SparkApplication.this.getString(R.string.app_developer_gmail_address), title, errorText.toString());

            defaultUncaughtExceptionHandler.uncaughtException(p1, p2);

            // prevent app from going to freeze state
            AndroidUtils.endApplication();
        };
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

}
