/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat.models;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class SparkUser {
	
	public static final String KEY_UID = "uid";
    public static final String KEY_NAME = "name";
	public static final String KEY_USERNAME = "username";
	public static final String KEY_PHOTOURL = "dp";
	public static final String KEY_IS_VERIFIED = "verified";
	public static final String KEY_LASTSEEN = "lastSeen";
	public static final String KEY_HOST = "host";
	
    public static SparkUser fromDataSnapshot(DataSnapshot p1){
        SparkUser user = p1.getValue(SparkUser.class);
        if(user != null){
            user.setUid(p1.getKey());
        }
        return user;
    }
	
	public static SparkUser fromJson(String json){
		SparkUser user = null;
		try{
			JSONObject obj = new JSONObject(json);
			user = new SparkUser(null, obj.getString(KEY_NAME), null);
			if(obj.has(KEY_UID)){
				user.setUid(obj.getString(KEY_UID));
			}
			
			if(obj.has(KEY_USERNAME)){
				user.setUsername(obj.getString(KEY_USERNAME));
			}
			
			if(obj.has(KEY_PHOTOURL)){
				user.setPhotoUrl(obj.getString(KEY_PHOTOURL));
			}
			
			if(obj.has(KEY_IS_VERIFIED)){
				user.setIsVerified(obj.getBoolean(KEY_IS_VERIFIED));
			}
			
			if(obj.has(KEY_LASTSEEN)){
				user.setLastSeen(obj.getLong(KEY_LASTSEEN));
			}
			
			if(obj.has(KEY_HOST)){
				user.setHost(obj.getString(KEY_HOST));
			}
		}catch(Exception e){}
		return user;
	}
    
    private String uid;
    private String name;
    private String username;
    private String photoUrl;
    private boolean isVerified;
    private Long lastSeen;
    private String host;

    public SparkUser(){
        isVerified = false;
    }

    public SparkUser(String username, @NonNull String name, String photoUrl) {
        this.username = username;
        this.name = name;
        this.photoUrl = photoUrl;
        this.isVerified = false;
    }

    @Exclude
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public boolean isVerified(){
        return isVerified;
    }
	
	public void setIsVerified(boolean isVerified){
		this.isVerified = isVerified;
	}

    public Long getLastSeen(){
        return lastSeen;
    }

    public void setLastSeen(Long lastSeen){
        this.lastSeen = lastSeen;
    }
    
    public String getHost(){
        return host;
    }
    
    public void setHost(String host){
        this.host = host;
    }
	
    public String toJson() {
        Map<String, Object> values = new HashMap<>();
		
		if(uid != null){
			values.put(KEY_UID, uid);
		}
		
		values.put(KEY_NAME, name);
		
		if(username != null){
			values.put(KEY_USERNAME, username);
		}
		
		if(photoUrl != null){
			values.put(KEY_PHOTOURL, photoUrl);
		}
		
		values.put(KEY_IS_VERIFIED, isVerified);
		
		if(lastSeen != null){
			values.put(KEY_LASTSEEN, lastSeen);
		}
		
		if(host != null){
			values.put(KEY_HOST, host);
		}
		
		return new JSONObject(values).toString();
    }
}
  
