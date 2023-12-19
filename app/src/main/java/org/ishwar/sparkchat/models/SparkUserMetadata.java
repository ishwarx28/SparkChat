/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat.models;
import com.google.firebase.database.Exclude;

public class SparkUserMetadata
{
    private String uid;
    private String description;
    private String countryCode;
    private String phone;
    private long creationTime;

    public SparkUserMetadata(){
        // Necessary for Firebase
    }

    public SparkUserMetadata(String description, String countryCode, String phone, long creationTime) {
        this.description = description; 
        this.countryCode = countryCode;
        this.phone = phone;
        this.creationTime = creationTime;
    }

    @Exclude
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
}
