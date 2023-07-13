package com.estudos.whatsapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.estudos.whatsapp.R;
import com.estudos.whatsapp.fragments.ContactsFragment;
import com.estudos.whatsapp.fragments.ChatsFragment;
import com.estudos.whatsapp.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int CHATS_FRAGMENT = 0;
    private static final int CONTACTS_FRAGMENT = 1;

    private MaterialSearchView materialSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();
    }

    private void initComponents() {

        //Configurar abas
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                        .add("Conversas", ChatsFragment.class)
                        .add("Contatos", ContactsFragment.class)
                        .create()
        );

        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        SmartTabLayout smartTabLayout = findViewById(R.id.viewPagerTab);
        smartTabLayout.setViewPager(viewPager);

        //toobar
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        toolbar.setTitle("WhatsApp");
        setSupportActionBar(toolbar);

        //pesquisa
        materialSearchView = findViewById(R.id.material_search_main);
        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    return false;
                }
                switch (viewPager.getCurrentItem()) {
                    case CHATS_FRAGMENT:
                        ChatsFragment chatsFragment = (ChatsFragment) adapter.getPage(CHATS_FRAGMENT);
                        if (TextUtils.isEmpty(newText)) {
                            chatsFragment.reloadChats();
                        } else {
                            chatsFragment.searchChats(newText.toLowerCase());
                        }

                        break;
                    case CONTACTS_FRAGMENT:
                        ContactsFragment contactsFragment = (ContactsFragment) adapter.getPage(CONTACTS_FRAGMENT);
                        if (TextUtils.isEmpty(newText)) {
                            contactsFragment.reloadContacts();
                        } else {
                            contactsFragment.searchContacts(newText.toLowerCase());
                        }
                        break;
                }
                return true;
            }
        });
        materialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                switch (viewPager.getCurrentItem()) {
                    case CHATS_FRAGMENT:
                        ChatsFragment chatsFragment = (ChatsFragment) adapter.getPage(CHATS_FRAGMENT);
                        chatsFragment.reloadChats();
                        break;
                    case CONTACTS_FRAGMENT:
                        ContactsFragment contactsFragment = (ContactsFragment) adapter.getPage(CONTACTS_FRAGMENT);
                        contactsFragment.reloadContacts();
                        break;
                }
            }
        });
    }

    private void signout() {
        FirebaseAuth firebaseAuth = FirebaseUtils.getAuth();
        try {
            firebaseAuth.signOut();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_toolbar_menu, menu);
        MenuItem item = menu.findItem(R.id.searchMenu);
        materialSearchView.setMenuItem(item);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signoutMenu:
                signout();
                break;
            case R.id.settingsMenu:
                goToSettings();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}