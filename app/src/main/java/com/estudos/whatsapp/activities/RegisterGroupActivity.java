package com.estudos.whatsapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.estudos.whatsapp.R;
import com.estudos.whatsapp.adapters.MembersSelectedAdapter;
import com.estudos.whatsapp.models.GroupModel;
import com.estudos.whatsapp.models.UserModel;
import com.estudos.whatsapp.utils.FirebaseUtils;
import com.estudos.whatsapp.utils.Refs;
import com.estudos.whatsapp.utils.UserUtils;
import com.estudos.whatsapp.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterGroupActivity extends AppCompatActivity {

    private static final String TAG = "RegisterGroupActivity";
    private List<UserModel> members = new ArrayList<>();
    private EditText groupName;
    private TextView register_members_total;
    private CircleImageView groupImage;
    private Toolbar toolbar;
    private FloatingActionButton fabRegistergroup;
    private RecyclerView recyclerViewMembers;
    private MembersSelectedAdapter membersAdapter;
    private StorageReference storageReference;
    private static final int SELECTION_GALLERY = 200;
    private static final int SELECTION_CAMERA = 100;

    private String[] requiredPermissions = new String[]{
            Manifest.permission.CAMERA
    };
    private GroupModel group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_group);


        if (getIntent().getExtras() != null) {
            List<UserModel> list = (List<UserModel>) getIntent().getExtras().getSerializable("members");
            members.addAll(list);
        }
        group = new GroupModel();


        initComponents();
        setListeners();
        setRecyclerview();

    }

    private void initComponents() {
        groupName = findViewById(R.id.group_name);
        register_members_total = findViewById(R.id.register_members_total);
        groupImage = findViewById(R.id.group_image);
        recyclerViewMembers = findViewById(R.id.recycler_view_register_members);
        fabRegistergroup = findViewById(R.id.fab_register_group);
        toolbar = findViewById(R.id.toobar_register_group);
        setSupportActionBar(toolbar);

        toolbar.setTitle("Novo Grupo");
        toolbar.setSubtitle("Defina o nome");
        register_members_total.setText("Participantes: " + members.size());

        storageReference = FirebaseUtils.getStorage();
    }

    private void setListeners() {
        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(view.getContext())
                        .setTitle("Selecionar foto da camera ou galeria")
                        .setPositiveButton("Camera", (a, b) -> {
                            boolean permissionCamera =   ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
                            if(permissionCamera){
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                if(intent.resolveActivity(getPackageManager())!=null){
                                    startActivityForResult(intent, SELECTION_CAMERA);
                                }else{
                                    Toast.makeText(getApplicationContext(), "Não foi possível abrir a camera!", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Utils.validPermissions(requiredPermissions, RegisterGroupActivity.this,1);
                            }
                        })
                        .setNegativeButton("Galeria", (a, b) -> {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            //se for possível abrir a camera
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(intent, SELECTION_GALLERY);
                            } else {
                                Toast.makeText(getApplicationContext(), "Não foi possível abrir a galeia.", Toast.LENGTH_SHORT).show();
                            }
                        }).create()
                        .show();

            }
        });

        fabRegistergroup.setOnClickListener(view -> {
            String name = groupName.getText().toString();
            if(TextUtils.isEmpty(name)){
                Toast.makeText(RegisterGroupActivity.this, "o nome do grupo não pode ser vazio!", Toast.LENGTH_LONG).show();
                return;
            }
            members.add(UserUtils.getDataCurrentUser());
            group.setMembers(members);
            group.setName(name);
            group.save();
            Intent i = new Intent(RegisterGroupActivity.this,ChatActivity.class);
            i.putExtra("chatGroup", group);
            startActivity(i);
        });
    }

    private void setRecyclerview() {
        membersAdapter = new MembersSelectedAdapter(members);
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerViewMembers.setLayoutManager(layoutManagerHorizontal);
        recyclerViewMembers.setHasFixedSize(true);
        recyclerViewMembers.setAdapter(membersAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        try {
            Bitmap bitmap = null;
            switch (requestCode) {
                case SELECTION_CAMERA:
                    bitmap = (Bitmap) data.getExtras().get("data");
                    break;

                case SELECTION_GALLERY:
                    Uri uri = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    break;
            }
            if (bitmap == null) {
                return;
            }
            groupImage.setImageBitmap(bitmap);
            uploadImageGroup(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadImageGroup(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] dataImage = baos.toByteArray();

        StorageReference imageRef = storageReference.child(Refs.IMAGES)
                .child(Refs.GROUPS)
                .child(group.getId() + ".jpeg");

        UploadTask uploadTask = imageRef.putBytes(dataImage);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterGroupActivity.this, "Erro ao fazer o uplaod da imagem do grupo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(RegisterGroupActivity.this, "Sucesso no upload da imagem do grupo!", Toast.LENGTH_SHORT).show();
                imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        String url = task.getResult().toString();
                        group.setPhoto(url);
                    }
                });
            }
        });

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
        android.app.AlertDialog.Builder builder= new android.app.AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar os recurso de camera é necessário aceitar as permissão.");
        builder.setPositiveButton("Entendi",null);
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}