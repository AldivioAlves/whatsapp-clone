package com.estudos.whatsapp.models;

import com.estudos.whatsapp.utils.FirebaseUtils;
import com.google.firebase.database.DatabaseReference;

public class ChatModel {
    private String userId;
    private String receiverId;
    private String lastMessage;
    private UserModel userToShowInChat;
    private String isGroup;
    private GroupModel group;

    public ChatModel(){
        this.setIsGroup("false");
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public UserModel getUserToShowInChat() {
        return userToShowInChat;
    }

    public String getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(String isGroup) {
        this.isGroup = isGroup;
    }

    public GroupModel getGroup() {
        return group;
    }

    public void setGroup(GroupModel group) {
        this.group = group;
    }

    public void setUserToShowInChat(UserModel userToShowInChat) {
        this.userToShowInChat = userToShowInChat;
    }

    public void save(){
        DatabaseReference ref = FirebaseUtils.getDatabase();
        DatabaseReference chatRef = ref.child("chats");
        chatRef.child(this.getUserId())
                .child(this.getReceiverId())
                .setValue(this);
    }
}
