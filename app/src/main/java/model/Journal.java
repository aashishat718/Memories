package model;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;

public class Journal implements Serializable {

    private String title;
    private String thoughts;
    private String imageUrl;
    private String userId;
    private String userName;
    private String docName,strTimeStamp;
    private String likeCount;
    private ArrayList<String> likedByList;
    private transient Timestamp timeAdded;

    public Journal() {
    }

    public Journal(String title, String thoughts, String imageUrl, String userId,
                   String userName, String docName, String strTimeStamp, String likeCount, ArrayList<String> likedByList, Timestamp timeAdded) {
        this.title = title;
        this.thoughts = thoughts;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.userName = userName;
        this.docName = docName;
        this.strTimeStamp = strTimeStamp;
        this.likeCount = likeCount;
        this.likedByList = likedByList;
        this.timeAdded = timeAdded;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThoughts() {
        return thoughts;
    }

    public void setThoughts(String thoughts) {
        this.thoughts = thoughts;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getStrTimeStamp() {
        return strTimeStamp;
    }

    public void setStrTimeStamp(String strTimeStamp) {
        this.strTimeStamp = strTimeStamp;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(String likeCount) {
        this.likeCount = likeCount;
    }

    public ArrayList<String> getLikedByList() {
        return likedByList;
    }

    public void setLikedByList(ArrayList<String> likedByList) {
        this.likedByList = likedByList;
    }
}
