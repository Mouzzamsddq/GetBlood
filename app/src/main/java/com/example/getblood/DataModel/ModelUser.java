package com.example.getblood.DataModel;

public class ModelUser {

    //use same name as firebase;

    String FullName, DateOfBirth, Blood, Contact, emailId, UID, profileImage, state, city, userType, pin, onlineStatus, typingTo,Register;  //addind two more fields
    boolean isBlocked = false;

    public ModelUser() {
    }

    public ModelUser(String fullName, String dateOfBirth, String blood, String contact, String emailId, String UID, String profileImage, String state, String city, String userType, String pin, String onlineStatus, String typingTo, String register, boolean isBlocked) {
        FullName = fullName;
        DateOfBirth = dateOfBirth;
        Blood = blood;
        Contact = contact;
        this.emailId = emailId;
        this.UID = UID;
        this.profileImage = profileImage;
        this.state = state;
        this.city = city;
        this.userType = userType;
        this.pin = pin;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
        Register = register;
        this.isBlocked = isBlocked;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getDateOfBirth() {
        return DateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        DateOfBirth = dateOfBirth;
    }

    public String getBlood() {
        return Blood;
    }

    public void setBlood(String blood) {
        Blood = blood;
    }

    public String getContact() {
        return Contact;
    }

    public void setContact(String contact) {
        Contact = contact;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public String getRegister() {
        return Register;
    }

    public void setRegister(String register) {
        Register = register;
    }
}


