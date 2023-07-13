package com.estudos.whatsapp.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtils {
    private static DatabaseReference databaseReference;
    private static FirebaseAuth auth;
    private static StorageReference firebaseStorage;

    public  static DatabaseReference getDatabase(){
        if(databaseReference==null){
           databaseReference = FirebaseDatabase.getInstance().getReference();
        }
        return databaseReference;
    }

    public static  FirebaseAuth getAuth(){
        if(auth==null){
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public static StorageReference getStorage(){
        if(firebaseStorage==null){
            firebaseStorage = FirebaseStorage.getInstance().getReference();
        }
        return firebaseStorage;
    }

    public static String getUserId(){
        if(auth==null){
            auth = FirebaseAuth.getInstance();
        }
        FirebaseUser user = auth.getCurrentUser();
        String email = user.getEmail();
        String id = Base64Utils.encode(email);
        return id;
    }
}
