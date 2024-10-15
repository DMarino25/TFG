package com.example.GameApp.ClassObjectes;

import com.google.firebase.Timestamp;

public class Reply {

    private String id;
    private String replyText;
    private String replyUserName;
    private String replyUserPicture;
    private Timestamp replyDate;

    public Reply() {
        // Constructor vacío necesario para Firebase
    }

    public Reply(String replyText, String replyUserName, String replyUserPicture, Timestamp replyDate) {
        this.replyText = replyText;
        this.replyUserName = replyUserName;
        this.replyUserPicture = replyUserPicture;
        this.replyDate = replyDate;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public String getReplyUserName() {
        return replyUserName;
    }

    public void setReplyUserName(String replyUserName) {
        this.replyUserName = replyUserName;
    }

    public String getReplyUserPicture() {
        return replyUserPicture;
    }

    public void setReplyUserPicture(String replyUserPicture) {
        this.replyUserPicture = replyUserPicture;
    }

    public Timestamp getReplyDate() {
        return replyDate;
    }

    public void setReplyDate(Timestamp replyDate) {
        this.replyDate = replyDate;
    }
}
