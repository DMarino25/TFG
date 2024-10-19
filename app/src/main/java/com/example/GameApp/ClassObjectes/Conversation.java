package com.example.GameApp.ClassObjectes;

import java.security.Timestamp;
import java.util.List;


public class Conversation {
    private String content;
    private List<String> participants;

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    private String conversationId;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    private boolean isRead;
    private Timestamp sentDate;
    private String userIdReceiver;
    private String userIdSender;
    private String receiverName; // Nuevo campo


    public Conversation() {
        // Constructor vacío necesario para Firebase
    }
    public Conversation(String conversationId, String userIdSender, String userIdReceiver, String receiverName, String content, boolean isRead) {
        this.conversationId = conversationId;
        this.userIdSender = userIdSender;
        this.userIdReceiver = userIdReceiver;
        this.receiverName = receiverName;
        this.content = content;
        this.isRead = isRead;
        this.participants = participants;
    }

    // Getters y setters

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public Timestamp getSentDate() {
        return sentDate;
    }

    public void setSentDate(Timestamp sentDate) {
        this.sentDate = sentDate;
    }

    public String getUserIdReceiver() {
        return userIdReceiver;
    }

    public void setUserIdReceiver(String userIdReceiver) {
        this.userIdReceiver = userIdReceiver;
    }

    public String getUserIdSender() {
        return userIdSender;
    }

    public void setUserIdSender(String userIdSender) {
        this.userIdSender = userIdSender;
    }

    // Nuevo getter y setter para receiverName
    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
}


