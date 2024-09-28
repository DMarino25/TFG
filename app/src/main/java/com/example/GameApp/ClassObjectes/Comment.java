package com.example.GameApp.ClassObjectes;

import com.google.firebase.Timestamp;

public class Comment {

    private String commentUserName;
    private String commentUserPicture;
    private String commentText;
    private Timestamp lastModifiedDate;
    private String _formattedDate; // Equals lastModifiedDate formatted and in String

    public Comment(String commentUserName, String commentUserPicture, String commentText, Timestamp lastModifiedDate) {
        this.commentUserName = commentUserName;
        this.commentUserPicture = commentUserPicture;
        this.commentText = commentText;
        this.lastModifiedDate = lastModifiedDate;
    }

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
