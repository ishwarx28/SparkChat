/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat.firebase;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.ishwar.sparkchat.models.SparkMessage;
import org.ishwar.sparkchat.utils.AndroidUtils;
import org.ishwar.sparkchat.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FirebaseUtils {

    private static FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private static DatabaseReference users_reference = mFirebaseDatabase.getReference("users");
    private static DatabaseReference metadata_reference = mFirebaseDatabase.getReference("metadata");
    private static DatabaseReference connections_reference = mFirebaseDatabase.getReference("connections");
    private static DatabaseReference chats_reference = mFirebaseDatabase.getReference("chats");

    @NonNull
    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public static boolean isLoggedIn() {
        return getAuth().getCurrentUser() != null;
    }

    public static String getUid() {
        return getAuth().getCurrentUser().getUid();
    }

    public static void logout(@NotNull Context context) {
        getAuth().signOut();
        FileUtils.deleteDir(new File(context.getApplicationInfo().dataDir).listFiles());
        FileUtils.deleteDir(context.getCacheDir().listFiles());
        AndroidUtils.restartApp(context);
    }

    public static FirebaseDatabase getDatabase() {
        return mFirebaseDatabase;
    }

    public static DatabaseReference getUserRef() {
        return users_reference;
    }

    public static DatabaseReference getMetadataRef() {
        return metadata_reference;
    }

    public static DatabaseReference getConnectionsRef() {
        return connections_reference;
    }

    public static DatabaseReference getChatsRef() {
        return chats_reference;
    }

    public static String combinedKey(String key1, String key2) {
        if (key1.equals(key2)) {
            return key1;
        } else if (key1.compareTo(key2) > key2.compareTo(key1)) {
            return key2 + key1;
        } else {
            return key1 + key2;
        }
    }

}