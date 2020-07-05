package com.example.getblood.DataModel;

public class ModelPost {
    //use same name as we given while uploading post
    String pID, pTitle, uDp, pLoc, pDescr, contact, uType, reqType, blood, uEmail, uName, uid, pInterest;

    public ModelPost() {
    }

    public ModelPost(String pID, String pTitle, String uDp, String pLoc, String pDescr, String contact, String uType, String reqType, String blood, String uEmail, String uName, String uid, String pInterest) {
        this.pID = pID;
        this.pTitle = pTitle;
        this.uDp = uDp;
        this.pLoc = pLoc;
        this.pDescr = pDescr;
        this.contact = contact;
        this.uType = uType;
        this.reqType = reqType;
        this.blood = blood;
        this.uEmail = uEmail;
        this.uName = uName;
        this.uid = uid;
        this.pInterest = pInterest;
    }

    public String getpID() {
        return pID;
    }

    public void setpID(String pID) {
        this.pID = pID;
    }

    public String getpTitle() {
        return pTitle;
    }

    public void setpTitle(String pTitle) {
        this.pTitle = pTitle;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getpLoc() {
        return pLoc;
    }

    public void setpLoc(String pLoc) {
        this.pLoc = pLoc;
    }

    public String getpDescr() {
        return pDescr;
    }

    public void setpDescr(String pDescr) {
        this.pDescr = pDescr;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getuType() {
        return uType;
    }

    public void setuType(String uType) {
        this.uType = uType;
    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public String getBlood() {
        return blood;
    }

    public void setBlood(String blood) {
        this.blood = blood;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getpInterest() {
        return pInterest;
    }

    public void setpInterest(String pInterest) {
        this.pInterest = pInterest;
    }
}
