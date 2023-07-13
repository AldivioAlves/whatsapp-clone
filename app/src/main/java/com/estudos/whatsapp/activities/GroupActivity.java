package com.estudos.whatsapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.estudos.whatsapp.adapters.ContactsAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.estudos.whatsapp.R;
import com.estudos.whatsapp.adapters.MembersSelectedAdapter;
import com.estudos.whatsapp.models.UserModel;
import com.estudos.whatsapp.utils.ClickListener;
import com.estudos.whatsapp.utils.FirebaseUtils;
import com.estudos.whatsapp.utils.UserUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {
    private RecyclerView recyclerViewMembersSelected, recyclerViewMembers;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private ContactsAdapter contactsAdapter;
    private MembersSelectedAdapter membersSelectedAdapter;
    private List<UserModel> members = new ArrayList<>();
    private List<UserModel> membersSelected = new ArrayList<>();
    private ValueEventListener usersListener;
    private DatabaseReference databaseReference;
    private static final String TAG = "GroupActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        initComponents();
        setListeners();
        setAdapters();
        setRecyclerViewMembers();
        setRecyclerViewMembersSelected();
    }


    private void initComponents() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Grupo");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fab = (FloatingActionButton) findViewById(R.id.fab_next);
        recyclerViewMembersSelected = findViewById(R.id.recycler_members_selecteds);
        recyclerViewMembers = findViewById(R.id.recycler_members);
        databaseReference = FirebaseUtils.getDatabase();
    }

    private  void updateMembersToolbar(){
        int totalMembersSelected = membersSelected.size();
        int total = members.size()+ membersSelected.size();
        toolbar.setSubtitle(totalMembersSelected+" de "+ total+" selecionados");
    }

    private void setListeners() {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(GroupActivity.this,RegisterGroupActivity.class);
                i.putExtra("members",(Serializable) membersSelected);
                startActivity(i);
            }
        });

    }

    private void setAdapters() {
        contactsAdapter = new ContactsAdapter(members);
        membersSelectedAdapter = new MembersSelectedAdapter(membersSelected);
    }

    private void setRecyclerViewMembers() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewMembers.setLayoutManager(layoutManager);
        recyclerViewMembers.setHasFixedSize(true);
        recyclerViewMembers.setAdapter(contactsAdapter);
        ClickListener clickListener = new ClickListener(
                getApplicationContext(),
                recyclerViewMembers,
                new ClickListener.OnItemClickListener(

                ) {
                    @Override
                    public void onItemClick(View view, int position) {
                        UserModel userSelected = members.get(position);
                        members.remove(userSelected);
                        contactsAdapter.notifyDataSetChanged();
                        membersSelected.add(userSelected);
                        membersSelectedAdapter.notifyDataSetChanged();
                        updateMembersToolbar();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    }
                }
        );
        recyclerViewMembers.addOnItemTouchListener(clickListener);

    }

    private void setRecyclerViewMembersSelected() {
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerViewMembersSelected.setLayoutManager(layoutManagerHorizontal);
        recyclerViewMembersSelected.setHasFixedSize(true);
        recyclerViewMembersSelected.setAdapter(membersSelectedAdapter);

        ClickListener clickListener = new ClickListener(getApplicationContext(), recyclerViewMembersSelected,
                new ClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        UserModel memberSelected = membersSelected.get(position);
                        membersSelected.remove(memberSelected);
                        membersSelectedAdapter.notifyDataSetChanged();
                        members.add(memberSelected);
                        contactsAdapter.notifyDataSetChanged();
                        updateMembersToolbar();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    }
                }
        );
        recyclerViewMembersSelected.addOnItemTouchListener(clickListener);
    }

    private void loadUsers() {
        usersListener = databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> allUsers = snapshot.getChildren();

                for (DataSnapshot userData : allUsers) {
                    UserModel user = userData.getValue(UserModel.class);
                    user.setId(userData.getKey());
                    if (!UserUtils.getUser().getEmail().equals(user.getEmail())) {
                        members.add(user);
                    }
                }
                contactsAdapter.notifyDataSetChanged();
                updateMembersToolbar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
