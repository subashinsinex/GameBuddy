package com.GameBuddy.gb;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Message {

    private String content;
    private String senderId;
    private String receiverId;
    private @ServerTimestamp Date timestamp;

    // Required empty constructor for Firestore
    public Message() {
    }

    public Message(String content, String senderId, String receiverId, Date timestamp) {
        this.content = content;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
