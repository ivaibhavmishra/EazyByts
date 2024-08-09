package com.example.myapplication;

public class Message {

    private String userId;
    private String email;
    private String message;

    // Constructor
    public Message(String userId, String email, String message) {
        this.userId = userId;
        this.email = email;
        this.message = message;
    }

    // Default constructor for Firebase
    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
