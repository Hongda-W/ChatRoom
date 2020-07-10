package com.hongdacode.chatroom;

public class Contacts
{
    private String Username, UserBio, profileImage;

    public Contacts(){

    }

    public Contacts(String Username, String UserBio, String profileImage) {
        this.Username = Username;
        this.UserBio = UserBio;
        this.profileImage = profileImage;
    }

    public String getUsername() {
        return this.Username;
    }

    public void setUsername(String Username) {
        this.Username = Username;
    }

    public String getUserBio() {
        return this.UserBio;
    }

    public void setUserBio(String UserBio) {
        this.UserBio = UserBio;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
