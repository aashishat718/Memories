package model;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {

    private String userId;
    private String userName;
    private String fullName;
    private String profilePicUrl;
    private ArrayList<String> friendList;
    private ArrayList<String> receivedRequestList;


    public User() {
    }

    public User(String userId, String userName, String fullName, String profilePicUrl, ArrayList<String> friendList, ArrayList<String> requestList) {
        this.userId = userId;
        this.userName = userName;
        this.fullName = fullName;
        this.profilePicUrl = profilePicUrl;
        this.friendList = friendList;
        this.receivedRequestList = requestList;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
