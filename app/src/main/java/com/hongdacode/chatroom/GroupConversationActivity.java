package com.hongdacode.chatroom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.acl.Group;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GroupConversationActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton mSendMessageButton;
    private EditText mMessageView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    private final List<Messages> mMessagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private GroupMessageAdaptor mMessageAdaptor;
    private RecyclerView mRecyclerView;

    private String mGroupName, mUserID, mUsername, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_conversation);

        mGroupName = getIntent().getStringExtra("GroupName");
        Toast.makeText(GroupConversationActivity.this, mGroupName, Toast.LENGTH_SHORT).show();

        mMessageAdaptor = new GroupMessageAdaptor(mMessagesList);
        mRecyclerView = findViewById(R.id.group_conv_recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mMessageAdaptor);

        mAuth = FirebaseAuth.getInstance();
        mUserID = mAuth.getCurrentUser().getUid();

        loadViews();

        getUserInfo();



        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessageToDatabase();
            }
        });

        // send message when "enter" is pressed on keyboard
        mMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                sendMessageToDatabase();
                return true;
            }
        });
    }

    @Override
    protected void onStart() {

        super.onStart();


        mDatabaseRef.child("Groups").child(mGroupName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages message = snapshot.getValue(Messages.class);

                mMessagesList.add(message);

                mMessageAdaptor.notifyDataSetChanged();

                mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount());
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


    private void getUserInfo() {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef.child("Users").child(mUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    mUsername = snapshot.child("Username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadViews() {
        mToolbar = findViewById(R.id.group_conversation_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(mGroupName);

        mSendMessageButton = findViewById(R.id.send_group_message);
        mMessageView = findViewById(R.id.group_message_input);

    }

    private void sendMessageToDatabase() {

        String messageInput = mMessageView.getText().toString();
        String messageKey = mDatabaseRef.child("Groups").child(mGroupName).push().getKey();

        if (messageInput.equals("")){
            Toast.makeText(this, "Please enter you message", Toast.LENGTH_SHORT).show();
        } else{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MM dd, hh:mm:ss.SSS");
            currentTime = dateFormat.format(Calendar.getInstance().getTime());

            HashMap<String, Object> groupMessageMap = new HashMap<>();
            mDatabaseRef.child("Groups").child(mGroupName).updateChildren(groupMessageMap);

            HashMap<String, Object> groupMessageDetail = new HashMap<>();
                groupMessageDetail.put("UserID", mUserID);
                groupMessageDetail.put("message", messageInput);
                groupMessageDetail.put("Time", currentTime);
                groupMessageDetail.put("type", "text");
            mDatabaseRef.child("Groups").child(mGroupName).child(messageKey).updateChildren(groupMessageDetail);
        }

        mMessageView.setText("");
    }

}