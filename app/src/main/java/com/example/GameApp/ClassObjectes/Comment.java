package com.example.GameApp.ClassObjectes;

import com.google.firebase.Timestamp;
import com.google.firebase.database.Exclude;

public class Comment {
    @Exclude
    private String id;
    private String commentUserName;
    private String commentUserPicture;
    private String commentText;
    private Timestamp lastModifiedDate;
    private String _formattedDate; // Equals lastModifiedDate formatted and in String

    public Comment() {
    }
    public Comment(String commentUserName, String commentUserPicture, String commentText, Timestamp lastModifiedDate) {
        this.commentUserName = commentUserName;
        this.commentUserPicture = commentUserPicture;
        this.commentText = commentText;
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }
    public String getCommentUserName() {
        return commentUserName;
    }

    public String getCommentUserPicture() {
        return commentUserPicture;
    }

    public String getCommentText() {
        return commentText;
    }

    public Timestamp getLastModifiedDate() {
        return lastModifiedDate;
    }
}
