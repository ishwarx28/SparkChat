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

import org.ishwar.sparkchat.firebase.FirebaseUtils;

public class SparkConnection {
    private String key;

    private String sender;
    private String receiver;
    private String lastMessage;
    private long sentTime;
    private int unreads;

    public SparkConnection(){

    }

    public SparkConnection(String sender, String receiver, String lastMessage, long sentTime, int unreads){
        this.sender = sender;
        this.receiver = receiver;
        this.key = FirebaseUtils.combinedKey(sender, receiver);
        this.lastMessage = lastMessage;
        this.sentTime = sentTime;
        this.unreads = unreads;
    }

    @Exclude
    public String getKey() {
        return key;
    }
    
    public void setKey(String key){
        this.key = key;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getLastMessage() {
        return lastMessage.toString();
    }
   
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getSentTime() {
        return sentTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    public int getUnreads() {
        return unreads;
    }

    public void setUnreads(int unreads) {
        this.unreads = unreads;
    }
}

