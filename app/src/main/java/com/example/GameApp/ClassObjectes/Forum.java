package com.example.GameApp.ClassObjectes;

import com.google.firebase.Timestamp;

public class Forum {
    private String id;
    private String title;
    private String description;
    private String userId;
    private Timestamp lastModifiedDate;
    private String _formattedDate; // Equals lastModifiedDate formatted and in String
    private String userName; // Obtained via userId
    private String userProfilePhoto; // Obtained via userId

    // Empty constructor
    public Forum() {}

    public Forum(String title, String description, String userId, Timestamp lastModifiedDate) {
        this.title = title;
        this.description = description;
        this.userId = userId;
        this.lastModifiedDate = lastModifiedDate;
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
}