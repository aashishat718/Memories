package util;

import android.app.Application;

import java.util.ArrayList;

public class JournalApi extends Application {

    private String username;
    private String userID;
    private String fullName;
    private String profilePicUrl;
    private ArrayList<String> friendList;
    private ArrayList<String> receivedRequestList;
    private static JournalApi instance;

    public static JournalApi getInstance(){
        if(instance==null)
            instance = new JournalApi();
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }


    public ArrayList<String> getFriendList() {
        return friendList;
    }

    public void setFriendList(ArrayList<String> friendList) {
        this.friendList = friendList;
    }


    public ArrayList<String> getReceivedRequestList() {
        return receivedRequestList;
    }

    public void setReceivedRequestList(ArrayList<String> receivedRequestList) {
        this.receivedRequestList = receivedRequestList;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }
}
