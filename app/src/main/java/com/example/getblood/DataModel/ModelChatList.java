package com.example.getblood.DataModel;

public class ModelChatList {
    String id; // we will neeed this id to get the chatr list,sender/receiver uid

    public ModelChatList(String id) {
        this.id = id;
    }

    public ModelChatList() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
