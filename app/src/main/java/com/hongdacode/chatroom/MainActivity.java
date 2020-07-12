package com.hongdacode.chatroom;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

        int[] myImageList = new int[]{R.drawable.conversation_frag, R.drawable.group_frag,
                R.drawable.contact_frag, R.drawable.friend_request};
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            mTabLayout.getTabAt(i).setIcon(myImageList[i]);
        }

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

        } else if (item.getItemId() == R.id.menu_new_group) {
          createNewGroup();

        } else if (item.getItemId() == R.id.menu_settings) {
            sendToSettingsActivity();

        } else if (item.getItemId() == R.id.menu_add_someone) {
            sendToNewFriendActivity();
        }
        return true;
    }

    private void createNewGroup() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        alertBuilder.setTitle("Enter group name: ");

        final EditText groupNameView = new EditText(MainActivity.this);
        groupNameView.setHint("e.g. Group1");
        alertBuilder.setView(groupNameView);

        alertBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameView.getText().toString();

                if (groupName.equals("")){
                    showErrorDialog("Group name cannot be empty");
                } else {
                    attemptNewGroup(groupName);
                }
            }
        });

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        alertBuilder.show();
    }

    private void attemptNewGroup(final String groupName) {
        mDatabaseRef.child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(groupName)){
                    showErrorDialog(groupName + " already exists!");
                }else {
                    mDatabaseRef.child("Groups").child(groupName).setValue("")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(MainActivity.this, groupName + " created sucessfully.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void sendToNewFriendActivity() {
        Intent newFriendIntent = new Intent(MainActivity.this, NewFriendActivity.class);
        startActivity(newFriendIntent);
    }

    private void showErrorDialog(String message){
        new android.app.AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}