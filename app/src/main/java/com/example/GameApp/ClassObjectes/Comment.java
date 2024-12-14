package com.example.GameApp.ClassObjectes;

import com.google.firebase.Timestamp;
import com.google.firebase.database.Exclude;

public class Comment {
    @Exclude
    private String id;
    private String commentUserNameId;
    private String commentUserName;
    private String commentUserPicture;
    private String commentText;
    private Timestamp lastModifiedDate;
    private String _formattedDate; // Equals lastModifiedDate formatted and in String
    private String _forumId;
    private boolean areRepliesVisible = false;

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

    public String getCommentUserNameId() { return commentUserNameId; }

    public void setCommentUserNameId(String commentUserNameId) { this.commentUserNameId = commentUserNameId; }

    public void setCommentUserName(String commentUserName) { this.commentUserName = commentUserName; }

    public String getCommentUserName() {
        return commentUserName;
    }

    public void setCommentUserPicture(String commentUserPicture) { this.commentUserPicture = commentUserPicture; }

    public String getCommentUserPicture() {
        return commentUserPicture;
    }

    public String getCommentText() {
        return commentText;
    }

    public Timestamp getLastModifiedDate() {
        return lastModifiedDate;
    }

    public String getUserNameId() {
        return commentUserNameId;
    }

    public void setUserNameId(String commentUserNameId) { this.commentUserNameId = commentUserNameId; }

    public String getForumId() {
        return _forumId;
    }

    public void setForumId(String forumId) { this._forumId = forumId; }

    public boolean areRepliesVisible() {
        return areRepliesVisible;
    }

    public void setRepliesVisible(boolean visible) {
        this.areRepliesVisible = visible;
    }
}
