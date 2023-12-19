/*
 * Copyright (c) 2023 Ishwar Meghwal
 *
 * All Right Reserved.
 *
 * SparkChat is a registered trademark of Ishwar Meghwal.
 * Unauthorized use or reproduction of this software or it's components prohibited by applicable law.
 */

package org.ishwar.sparkchat.models;
import java.util.ArrayList;
import java.util.List;

public class SparkGroup extends SparkConnection{
    
    private List<String> members = null;
    
    public SparkGroup(){
        super();
    }

    public SparkGroup(String sender, String receiver, String lastMessage, long sentTime, int unreads){
        super(sender, receiver, lastMessage, sentTime, unreads);
        setKey(receiver);
    }
    
    public List<String> getMembers(){
        return members;
    }
    
    public void setMembers(ArrayList<String> members){
        this.members = members;
    }
    
    public void addMember(String uid){
        if(members == null){
            members = new ArrayList<>();
        }
        if(!members.contains(uid)){
            members.add(uid);
        }
    }
}
