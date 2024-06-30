package com.GameBuddy.gb;

import java.util.Date;

public class Message {
    private String content;
    private String senderId;
    private String receiverId;
    private Date timestamp;

    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
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

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
