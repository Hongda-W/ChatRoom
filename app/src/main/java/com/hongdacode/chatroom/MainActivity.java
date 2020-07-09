package com.hongdacode.chatroom;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabAdaptor mTabAdaptor;

    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = findViewById(R.id.main_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ChatRoom");

        mViewPager = findViewById(R.id.mainTabsPager);
        mTabAdaptor = new TabAdaptor(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mViewPager.setAdapter(mTabAdaptor);

        mTabLayout = findViewById(R.id.mainTabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void onStart() {

        super.onStart();

        if (user == null){
            sendToLoginActivity();
        }
        else {
            isUserValid();
        }
    }

    private void isUserValid() {
        String userID = mAuth.getCurrentUser().getUid();

        mDatabaseRef.child("Users").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.child("Username").exists())) {
                    Toast.makeText(MainActivity.this, "Welcome: " + snapshot.child("Username").toString(), Toast.LENGTH_SHORT);
                } else {
                    sendToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_options, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.menu_logout) {
            mAuth.signOut();
            sendToLoginActivity();
        }
        else if (item.getItemId() == R.id.menu_settings) {
            sendToSettingsActivity();
        }
        else if (item.getItemId() == R.id.menu_add_someone) {
        }
        return true;
    }

    private void sendToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        finish();
    }
}