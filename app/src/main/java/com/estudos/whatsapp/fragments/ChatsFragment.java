package com.estudos.whatsapp.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.estudos.whatsapp.R;
import com.estudos.whatsapp.activities.ChatActivity;
import com.estudos.whatsapp.adapters.ChatsAdapter;
import com.estudos.whatsapp.models.ChatModel;
import com.estudos.whatsapp.models.UserModel;
import com.estudos.whatsapp.utils.Base64Utils;
import com.estudos.whatsapp.utils.ClickListener;
import com.estudos.whatsapp.utils.FirebaseUtils;
import com.estudos.whatsapp.utils.Refs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<ChatModel> chats = new ArrayList<>();
    private DatabaseReference databaseReference;
    private ValueEventListener chatsListener;
    private ChatsAdapter chatsAdapter;

    public ChatsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        initComponents(view);
        setupRecyclerView(view);
        setListeners();
        return view;
    }

    private void initComponents(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewChats);
    }

    private void setupRecyclerView(View view) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        chatsAdapter = new ChatsAdapter(chats);
        recyclerView.setAdapter(chatsAdapter);
        recyclerView.setHasFixedSize(true);
    }

    private void setListeners() {
        ClickListener clickListener = new ClickListener(getActivity(), recyclerView,
                new ClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        List<ChatModel> chatUpdated = chatsAdapter.getChats();
                        ChatModel chat = chatUpdated.get(position);
                        Intent intent = new Intent(getActivity(), ChatActivity.class);

                        if (chat.getIsGroup().equals("true")) {
                            intent.putExtra("chatGroup", chat.getGroup());
                        } else {
                            intent.putExtra("user", chat.getUserToShowInChat());
                        }

                        startActivity(intent);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                });

        recyclerView.addOnItemTouchListener(clickListener);
    }

    public void searchChats(String text) {
        List<ChatModel> result = new ArrayList<>();
        for (ChatModel chat : chats) {

            if (chat.getUserToShowInChat() != null) { // conversa convencional
                String userName = chat.getUserToShowInChat().getName().toLowerCase();
                String lastMessage = chat.getLastMessage().toLowerCase();
                if (userName.contains(text) || lastMessage.contains(text)) {
                    result.add(chat);
                }
            } else { // conversa de grupo
                String userName = chat.getGroup().getName().toLowerCase();
                String lastMessage = chat.getLastMessage().toLowerCase();
                if (userName.contains(text) || lastMessage.contains(text)) {
                    result.add(chat);
                }
            }

        }
        chatsAdapter = new ChatsAdapter(result);
        recyclerView.setAdapter(chatsAdapter);
        chatsAdapter.notifyDataSetChanged();
    }

    public void reloadChats() {
        chatsAdapter = new ChatsAdapter(chats);
        recyclerView.setAdapter(chatsAdapter);
        chatsAdapter.notifyDataSetChanged();
    }

    private void loadChats() {
        databaseReference = FirebaseUtils.getDatabase();
        chatsListener = databaseReference.child(Refs.CHATS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chats.clear();
                        Iterable<DataSnapshot> allChats = snapshot.getChildren();
                        for (DataSnapshot chatData : allChats) {
                            if (chatData.getKey().equals(FirebaseUtils.getUserId())) {
                                Iterable<DataSnapshot> allUserChats = chatData.getChildren();
                                for (DataSnapshot userChatData : allUserChats) {
                                    ChatModel chat = userChatData.getValue(ChatModel.class);
                                    if (chat.getUserToShowInChat() != null) {
                                        chat.getUserToShowInChat().setId(Base64Utils.encode(
                                                chat.getUserToShowInChat().getEmail()
                                        ));
                                    }
                                    chats.add(chat);
                                }
                            }
                        }
                        chatsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        loadChats();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (chatsListener != null) {
            databaseReference.removeEventListener(chatsListener);
        }
    }
}