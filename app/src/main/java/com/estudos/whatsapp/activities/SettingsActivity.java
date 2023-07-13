package com.estudos.whatsapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.estudos.whatsapp.R;
import com.estudos.whatsapp.models.UserModel;
import com.estudos.whatsapp.utils.FirebaseUtils;
import com.estudos.whatsapp.utils.UserUtils;
import com.estudos.whatsapp.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private static  final int SELECTION_CAMERA = 100;
    private static  final int SELECTION_GALLERY = 200;

    private EditText editName;
    private ImageView btnEdit;
    private DatabaseReference databaseReference;
    private String userId;
    private FirebaseUser user;
    private  UserModel userModel;
    private  StorageReference storageReference;
    private ValueEventListener valueEventListenerUser;
    private ImageButton btnCamera;
    private ImageButton btnGallery;
    private CircleImageView imageProfile;


    private String[] requiredPermissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initComponents();
        loadUserInformation();
        initListeners();
        //validar permissões
        Utils.validPermissions(requiredPermissions, this,1);
    }

    private void initComponents(){
        databaseReference =  FirebaseUtils.getDatabase();
        userId = UserUtils.getUserId();
        storageReference = FirebaseUtils.getStorage();
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        editName = findViewById(R.id.editName);
        btnEdit = findViewById(R.id.btnEdit);
        btnCamera = findViewById(R.id.imageButtonCamera);
        btnGallery = findViewById(R.id.imageButtonGallery);
        imageProfile = findViewById(R.id.image_profile);

        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void initListeners(){
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editName.getText().toString();
                if(!TextUtils.isEmpty(name)){
                    editNameVisualization();
                }else{
                    Toast.makeText(getApplicationContext(), "É necessário preencher o campo de nome!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //se for possível abrir a camera
                if(intent.resolveActivity(getPackageManager())!= null){
                    startActivityForResult(intent,SELECTION_CAMERA);  
                }else{
                    Toast.makeText(getApplicationContext(), "Não foi possível abrir a camera.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //se for possível abrir a camera
                if(intent.resolveActivity(getPackageManager())!= null){
                    startActivityForResult(intent,SELECTION_GALLERY);
                }else{
                    Toast.makeText(getApplicationContext(), "Não foi possível abrir a galeia.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void editNameVisualization(){
        String name = editName.getText().toString();
        boolean success = UserUtils.updateDisplayUserName(name);
        if(success){
            Toast.makeText(getApplicationContext(), "Sucesso em atualizar o nome de perfil", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "Erro ao atualizar o nome de perfil", Toast.LENGTH_SHORT).show();
        }
        userModel.setName(name);
        userModel.updateUser();
        
    }

    private void loadUserInformation(){

        userModel= UserUtils.getDataCurrentUser();

        user = UserUtils.getUser();
        editName.setText(user.getDisplayName());
        Uri uri = user.getPhotoUrl();
        if(uri!=null){
            Glide.with(SettingsActivity.this)
                    .load(uri)
                    .into(imageProfile);
        }else{
            imageProfile.setImageResource(R.drawable.default_user_image);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(valueEventListenerUser!=null){
            databaseReference.removeEventListener(valueEventListenerUser);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int resultPermission: grantResults){
            if(resultPermission != PackageManager.PERMISSION_GRANTED){
                alertPermissionsNotGranted();
            }
        }
    }

    private void alertPermissionsNotGranted(){

        DialogInterface.OnClickListener positiveAction = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar os recursos dessa tela é necessário aceitar as permissões.");
        builder.setPositiveButton("Confirmar",positiveAction);
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode!=RESULT_OK)return;

        Bitmap bitmap = null;

        try {
            switch (requestCode){
                case SELECTION_CAMERA:
                    bitmap =(Bitmap) data.getExtras().get("data");
                    break;

                case SELECTION_GALLERY:
                    Uri uri = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                    break;
            }
            if(bitmap !=null){
                imageProfile.setImageBitmap(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); 
                byte[] dataImage = baos.toByteArray();


                StorageReference imagemRef = storageReference
                        .child("images")
                        .child("profile")
                        .child(userId)
                        .child("perfil.jpeg");

                UploadTask uploadTask = imagemRef.putBytes(dataImage);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Erro ao fazer upload da imagem!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(), "Sucesso no upload da imagem!", Toast.LENGTH_SHORT).show();
                        imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                              Uri uri = task.getResult();
                              updatePhotoUser(uri);
                            }
                        });
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updatePhotoUser(Uri uri){
       boolean success =  UserUtils.updatePhotoUser(uri);
       if(success){
           Toast.makeText(getApplicationContext(), "Sua foto foi atualizada!!", Toast.LENGTH_SHORT).show();
       }else{
           Toast.makeText(getApplicationContext(), "Sua foto não foi alterada!", Toast.LENGTH_SHORT).show();
       }
        userModel.setPhoto(uri.toString());
        userModel.updateUser();
    }
}