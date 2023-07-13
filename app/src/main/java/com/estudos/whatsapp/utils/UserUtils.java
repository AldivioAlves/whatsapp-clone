package com.estudos.whatsapp.utils;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.estudos.whatsapp.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UserUtils {
    public static String getUserId() {
        FirebaseAuth auth = FirebaseUtils.getAuth();
        FirebaseUser user = auth.getCurrentUser();
        String email = user.getEmail();
        String id = Base64Utils.encode(email);
        return id;
    }

    public static FirebaseUser getUser() {
        FirebaseAuth auth = FirebaseUtils.getAuth();
        FirebaseUser user = auth.getCurrentUser();
        return user;
    }

    public static boolean updatePhotoUser(Uri uri) {

        try {
            FirebaseUser user = getUser();

            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(uri)
                    .build();

            user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean updateDisplayUserName(String name) {
        try {
            FirebaseUser user = getUser();

            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();

            user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static UserModel getDataCurrentUser() {
        FirebaseAuth auth = FirebaseUtils.getAuth();
        UserModel userModel = new UserModel();
        userModel.setEmail(getUser().getEmail());
        userModel.setName(getUser().getDisplayName());

        if (getUser().getPhotoUrl() == null) {
            userModel.setPhoto("");
        } else {
            userModel.setPhoto(getUser().getPhotoUrl().toString());
        }
        return userModel;
    }
}
