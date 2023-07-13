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
import com.estudos.whatsapp.activities.GroupActivity;
import com.estudos.whatsapp.adapters.ChatsAdapter;
import com.estudos.whatsapp.adapters.ContactsAdapter;
import com.estudos.whatsapp.models.ChatModel;
import com.estudos.whatsapp.models.UserModel;
import com.estudos.whatsapp.utils.ClickListener;
import com.estudos.whatsapp.utils.FirebaseUtils;
import com.estudos.whatsapp.utils.UserUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<UserModel> contacts = new ArrayList<>();
    private DatabaseReference databaseReference;
    private ValueEventListener usersListener;
    private ContactsAdapter contactsAdapter;

    public ContactsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        initComponentes(view);
        return view;
    }

    private void addHeaderNewGroup(){
        UserModel header = new UserModel();
        header.setName("Novo Grupo");
        header.setEmail("");
        contacts.add(header);
    }

    private void clearContacts(){
        contacts.clear();
        addHeaderNewGroup();
    }

    private void loadUsers() {
        usersListener = databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                clearContacts();
                Iterable<DataSnapshot> allUsers = snapshot.getChildren();
                for (DataSnapshot userData : allUsers) {
                    UserModel user = userData.getValue(UserModel.class);
                    user.setId(userData.getKey());
                    if (!UserUtils.getUser().getEmail().equals(user.getEmail())) {
                        contacts.add(user);
                    }
                }
                contactsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void initComponentes(View view) {
        recyclerView = view.findViewById(R.id.recycleContacts);
        databaseReference = FirebaseUtils.getDatabase();
        contactsAdapter = new ContactsAdapter(contacts);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(contactsAdapter);
        recyclerView.setHasFixedSize(true);
        //recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), LinearLayout.VERTICAL));
        recyclerView.addOnItemTouchListener(
                new ClickListener(
                        getActivity(),
                        recyclerView,
                        new ClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                List<UserModel> contactsUpdated = contactsAdapter.getContacts();
                                UserModel user = contactsUpdated.get(position);
                                boolean isHeader = TextUtils.isEmpty(user.getEmail());
                                Intent intent;
                                if (isHeader) {
                                    intent = new Intent(getActivity(), GroupActivity.class);
                                } else {
                                    intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("user", user);
                                }

                                startActivity(intent);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );
        addHeaderNewGroup();
    }

    public void searchContacts(String text) {
        List<UserModel> result = new ArrayList<>();
        for (UserModel contact : contacts) {
            String userName = contact.getName().toLowerCase();
            if (userName.contains(text)) {
                result.add(contact);
            }
        }
        contactsAdapter = new ContactsAdapter(result);
        recyclerView.setAdapter(contactsAdapter);
        contactsAdapter.notifyDataSetChanged();
    }

    public void reloadContacts() {
        contactsAdapter = new ContactsAdapter(contacts);
        recyclerView.setAdapter(contactsAdapter);
        contactsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadUsers();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (usersListener != null) {
            databaseReference.removeEventListener(usersListener);
        }
    }
}