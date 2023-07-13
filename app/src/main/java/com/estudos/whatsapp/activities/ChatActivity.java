package com.estudos.whatsapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.estudos.whatsapp.R;
import com.estudos.whatsapp.adapters.MessagesAdapter;
import com.estudos.whatsapp.models.ChatModel;
import com.estudos.whatsapp.models.GroupModel;
import com.estudos.whatsapp.models.MessageModel;
import com.estudos.whatsapp.models.UserModel;
import com.estudos.whatsapp.utils.Base64Utils;
import com.estudos.whatsapp.utils.FirebaseUtils;
import com.estudos.whatsapp.utils.Refs;
import com.estudos.whatsapp.utils.UserUtils;
import com.estudos.whatsapp.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private FloatingActionButton fabSendMessage;
    private static final int SELECTION_CAMERA = 100;
    private Toolbar toolbar;
    private CircleImageView imageProfile;
    private ImageView imageCamera;
    private UserModel userReceiver;
    private String userReceiverId;
    private EditText editMessage;
    private List<MessageModel> messages = new ArrayList<>();
    private MessagesAdapter messagesAdapter;
    private RecyclerView recyclerViewMessages;
    private DatabaseReference databaseReference;
    private DatabaseReference messagesRef;
    private ChildEventListener childEventListenerMessages;
    private GroupModel group;

    private String[] requiredPermissions = new String[]{
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initComponents();
        setChat();
        setListeners();
    }

    private void initComponents() {
        editMessage = findViewById(R.id.editMessage);
        imageCamera = findViewById(R.id.imageCamera);
        toolbar = findViewById(R.id.mainToolbar);
        toolbar.setTitle("");
        toolbar.setTitleMarginStart(140);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        imageProfile = findViewById(R.id.image_profile);
        fabSendMessage = findViewById(R.id.fab_send_message);

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        messagesAdapter = new MessagesAdapter(messages, getApplicationContext());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messagesAdapter);
        recyclerViewMessages.setHasFixedSize(true);
    }

    private void setChat() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String photo;
            if (bundle.containsKey("chatGroup")) {
                group = (GroupModel) bundle.getSerializable("chatGroup");
                toolbar.setTitle(group.getName());
                photo = group.getPhoto();
                userReceiverId = group.getId();
            } else {
                userReceiver = (UserModel) bundle.getSerializable("user");
                userReceiverId = userReceiver.getId();
                toolbar.setTitle(userReceiver.getName());
                photo = userReceiver.getPhoto();
            }

            if (photo != null) {
                Glide.with(ChatActivity.this)
                        .load(Uri.parse(photo))
                        .into(imageProfile);
            } else {
                imageProfile.setImageResource(R.drawable.default_user_image);
            }
        }
        databaseReference = FirebaseUtils.getDatabase();
        messagesRef = databaseReference.child(Refs.MESSAGES)
                .child(UserUtils.getUserId())
                .child(userReceiverId);
    }

    private void setListeners() {
        fabSendMessage.setOnClickListener((view -> {
            String message = editMessage.getText().toString();
            MessageModel messageModel = new MessageModel();
            String userId = UserUtils.getUserId();
            messageModel.setUserId(userId);
            messageModel.setMessage(message);
            if (TextUtils.isEmpty(message)) {
                return;
            }
            if (userReceiver != null) { // mensagem convencional
                //salvar mensagem para quem envia e para quem recebe
                saveMessage(userId, userReceiverId, messageModel);
                // Salvar a conversa para quem envia e para quem recebe
                saveChat(userId, userReceiverId, userReceiver, messageModel, false);
                saveChat(userReceiverId, userId, UserUtils.getDataCurrentUser(), messageModel, false);
            } else { // mensagem de grupo
                for (UserModel member : group.getMembers()) {
                    messageModel.setName(UserUtils.getDataCurrentUser().getName());
                    String receiverGroupId = Base64Utils.encode(member.getEmail());
                    saveMessage(receiverGroupId, userReceiverId, messageModel);
                    saveChat(receiverGroupId, userReceiverId, userReceiver, messageModel, true);
                }
            }
        }));

        imageCamera.setOnClickListener(v -> {
            boolean permissionCamera = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            if (permissionCamera) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, SELECTION_CAMERA);
                } else {
                    Toast.makeText(getApplicationContext(), "Não foi possível abrir a camera!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Utils.validPermissions(requiredPermissions, ChatActivity.this, 1);
            }
        });
    }

    public void saveMessage(String userId, String receiverId, MessageModel message) {
        DatabaseReference databaseReference = FirebaseUtils.getDatabase();
        DatabaseReference messagesRef = databaseReference.child(Refs.MESSAGES);
        messagesRef.child(receiverId).child(userId).push().setValue(message);
        messagesRef.child(userId).child(receiverId).push().setValue(message);
        editMessage.setText("");
    }

    public void saveChat(String userId, String receiverId, UserModel userToShow, MessageModel message, boolean isGroup) {
        ChatModel chat = new ChatModel();
        chat.setUserId(userId);
        chat.setReceiverId(receiverId);
        chat.setLastMessage(message.getMessage());
        if (isGroup) {
            chat.setIsGroup("true");
            chat.setGroup(group);
        } else {
            chat.setUserToShowInChat(userToShow);
        }
        chat.save();
    }

    private void getMessages() {
        messages.clear();
        childEventListenerMessages = messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessageModel messageModel = snapshot.getValue(MessageModel.class);
                messages.add(messageModel);
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getMessages();
    }

    @Override
    protected void onStop() {
        super.onStop();
        messagesRef.removeEventListener(childEventListenerMessages);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        Bitmap bitmap = null;
        try {
            if (requestCode == SELECTION_CAMERA) {
                bitmap = (Bitmap) data.getExtras().get("data");
            }
            if (bitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] dataImage = baos.toByteArray();

                //criar nome da imagem
                String imageName = UUID.randomUUID().toString();
                //criando referencia da imagem no firebase
                StorageReference storageReference = FirebaseUtils.getStorage();
                final StorageReference imagemRef = storageReference.child("images")
                        .child("photos")
                        .child(UserUtils.getUserId())
                        .child(imageName);

                UploadTask uploadTask = imagemRef.putBytes(dataImage);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Erro ao fazer upload da imagem!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                Uri uri = task.getResult();
                                MessageModel messageModel = new MessageModel();
                                messageModel.setUserId(UserUtils.getUserId());
                                messageModel.setMessage("image.jpeg");
                                messageModel.setImage(uri.toString());

                                if (userReceiver != null) {
                                    saveMessage(UserUtils.getUserId(), userReceiverId, messageModel);
                                } else {
                                    for (UserModel member : group.getMembers()) {
                                        messageModel.setName(UserUtils.getDataCurrentUser().getName());
                                        String receiverGroupId = Base64Utils.encode(member.getEmail());
                                        saveMessage(receiverGroupId, userReceiverId, messageModel);
                                        saveChat(receiverGroupId, userReceiverId, userReceiver, messageModel, true);
                                    }
                                }

                                Toast.makeText(getApplicationContext(), "Sucesso ao enviar imagem!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int resultPermission : grantResults) {
            if (resultPermission != PackageManager.PERMISSION_GRANTED) {
                alertPermissionsNotGranted();
            }
        }
    }

    private void alertPermissionsNotGranted() {

        DialogInterface.OnClickListener positiveAction = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar os recurso de camera é necessário aceitar as permissão.");
        builder.setPositiveButton("Entendi", null);
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}