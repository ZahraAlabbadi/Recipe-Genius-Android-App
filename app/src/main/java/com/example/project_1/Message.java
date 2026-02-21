package com.example.project_1;

import java.io.Serializable;

public class Message implements Serializable{
    public static final String SEND_BY_ME = "user";
    public static final String SEND_BY_BOT = "assistant";

    private String message;
    private String sentBy;
    private String timestamp;

    public Message(String message, String sentBy, String timestamp) {
        this.message = message;
        this.sentBy = sentBy;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getSentBy() {
        return sentBy;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
