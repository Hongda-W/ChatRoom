package com.hongdacode.chatroom;

public class Messages {
    private String message, UserID, type;

    public Messages (){

    }

    public Messages(String message, String UserID, String type) {
        this.message = message;
        this.UserID = UserID;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUsername(String username) {
        this.UserID = UserID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
