package com.estudos.whatsapp.models;

import android.widget.Toast;

import com.estudos.whatsapp.utils.Base64Utils;
import com.estudos.whatsapp.utils.FirebaseUtils;
import com.estudos.whatsapp.utils.UserUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserModel implements Serializable {
    private String name, email,id, password, photo;

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public UserModel() {
    }

    @Exclude
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void saveUser(){
        String id = Base64Utils.encode(this.email);
        DatabaseReference usersRef = FirebaseUtils.getDatabase().child("users");
        usersRef.child(id).setValue(this);

    }

    public void updateUser(){
        String userId = UserUtils.getUserId();
        DatabaseReference userRef= FirebaseUtils.getDatabase().child("users").child(userId);

        Map<String,Object> userValues= convertToMap();
        userRef.updateChildren(userValues);

    }

    @Exclude
    public Map<String,Object> convertToMap(){
        HashMap<String,Object> usermap = new HashMap<>();
        usermap.put("email", getEmail());
        usermap.put("name", getName());
        usermap.put("photo", getPhoto());
        return usermap;
    }
}
