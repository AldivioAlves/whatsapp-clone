package com.estudos.whatsapp.models;

import android.provider.ContactsContract;

import com.estudos.whatsapp.utils.Base64Utils;
import com.estudos.whatsapp.utils.FirebaseUtils;
import com.estudos.whatsapp.utils.Refs;
import com.estudos.whatsapp.utils.UserUtils;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.List;

public class GroupModel implements Serializable {
    private String id, name, photo;
    private List<UserModel> members;

    public GroupModel(){
        DatabaseReference databaseReference = FirebaseUtils.getDatabase();
        DatabaseReference ref = databaseReference.child(Refs.GROUPS);
        String groupId = ref.push().getKey();
        setId(groupId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<UserModel> getMembers() {
        return members;
    }

    public void setMembers(List<UserModel> members) {
        this.members = members;
    }

    public void save() {
        DatabaseReference databaseReference = FirebaseUtils.getDatabase();
        DatabaseReference ref = databaseReference.child(Refs.GROUPS);
        ref.child(getId()).setValue(this);

        for(UserModel member: getMembers()){ // criar esse grupo (conversa) para cada membro
            String receivedId = Base64Utils.encode(member.getEmail());
            ChatModel chat = new ChatModel();
            chat.setUserId(receivedId);
            chat.setReceiverId(getId());
            chat.setLastMessage("");
            chat.setIsGroup("true");
            chat.setGroup(this);
            chat.save();
        }
    }
}
