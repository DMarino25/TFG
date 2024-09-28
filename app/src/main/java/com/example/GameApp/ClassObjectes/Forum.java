package com.example.GameApp.ClassObjectes;

import com.google.firebase.Timestamp;

import java.util.Map;

public class Forum {
    private String id;
    private String title;
    private String description;
    private String userId;
    private Timestamp lastModifiedDate;
    private String _formattedDate; // Equals lastModifiedDate formatted and in String
    private String userName;
    private String userProfilePhoto;
    private int likeCount;
    private int dislikeCount;
    private Map<String, Boolean> userLikes; // True for like, false for dislike


    // Empty constructor
    public Forum() {}

    public Forum(String title, String description, String userId, Timestamp lastModifiedDate, int likeCount, int dislikeCount)   {
        this.title = title;
        this.description = description;
        this.userId = userId;
        this.lastModifiedDate = lastModifiedDate;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUserId() { return userId; }

    public Timestamp getLastModifiedDate() { return lastModifiedDate; }

    public String getUserName() { return userName; }

    public void setUserName(String userName) { this.userName = userName; }

    public String getUserProfilePhoto() { return userProfilePhoto; }

    public void setUserProfilePhoto(String userProfilePhoto) { this.userProfilePhoto = userProfilePhoto; }

    public String getFormattedDate() { return _formattedDate; }

    public void setFormattedDate(String formattedDate) { this._formattedDate = formattedDate; }

    // Constructor, getters y setters
    public int getLikeCount() { return likeCount; }

    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getDislikeCount() { return dislikeCount; }

    public void setDislikeCount(int dislikeCount) { this.dislikeCount = dislikeCount; }

    public Map<String, Boolean> getUserLikes() { return userLikes; }

    public void setUserLikes(Map<String, Boolean> userLikes) { this.userLikes = userLikes; }
}