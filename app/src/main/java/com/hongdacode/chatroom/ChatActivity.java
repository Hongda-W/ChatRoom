package com.hongdacode.chatroom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton mSendMessageButton;
    private ScrollView mScrollView;
    private TextView mTextView;
    private EditText mEditText;

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;

    private String conversationID, myUserID, myUsername, theirUserID, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        loadViews();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        myUserID = mAuth.getCurrentUser().getUid();

        theirUserID = getIntent().getStringExtra("theirUserID");

        getUserInfo();

        mDatabaseReference.child("ConversationIndex").child(myUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final String convID;
                if (snapshot.exists() && snapshot.hasChild(theirUserID) &&
                        snapshot.child(theirUserID).hasChild("id") ){
                    convID = snapshot.child(theirUserID).child("id").getValue().toString();
                } else{
                    createChat(myUserID, theirUserID);
                    convID = conversationID;
                    mDatabaseReference.child("ConversationIndex").child(myUserID).child(theirUserID).child("id").setValue(conversationID);
                    mDatabaseReference.child("ConversationIndex").child(theirUserID).child(myUserID).child("id").setValue(conversationID);
                    mDatabaseReference.child("Conversations").child(convID).setValue("")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(ChatActivity.this,  "Chat with ID: "+ conversationID+ "created successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

                mSendMessageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendMessageToDatabase(convID);
                    }
                });

                // send message when "enter" is pressed on keyboard
                mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        sendMessageToDatabase(convID);
                        return true;
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDatabaseReference.child("Users").child(myUserID).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    final String mUsername = snapshot.child("Username").getValue().toString();

                    mDatabaseReference.child("Users").child(theirUserID).getRef().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                final String tUsername =snapshot.child("Username").getValue().toString();

                                getSupportActionBar().setTitle(tUsername);

                                Toast.makeText(ChatActivity.this, mUsername + "'s conversation with "+tUsername, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabaseReference.child("ConversationIndex").child(myUserID).child(theirUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("id") ){
                    final String convID = snapshot.child("id").getValue().toString();
                    mDatabaseReference.child("Conversations").child(convID).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            if (snapshot.exists()){
                                displayMessages(snapshot);
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            if (snapshot.exists()){
                                displayMessages(snapshot);
                            }

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createChat(final String myUserID, final String theirUserID) {
        // TODO: create chat for the two users on database
        conversationID = createTransactionID();
        mDatabaseReference.child("Conversations").getRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild(conversationID)){
                    createChat(myUserID, theirUserID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public String createTransactionID(){
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    private void loadViews() {
        mToolbar = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(mToolbar);

        mSendMessageButton = findViewById(R.id.send_chat_message);
        mEditText = findViewById(R.id.chat_message_input);

        mTextView = findViewById(R.id.chat_text);
        mScrollView = findViewById(R.id.chat_scroll_view);
    }

    private void getUserInfo() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(myUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    myUsername = snapshot.child("Username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessageToDatabase(String convID) {
        String messageInput = mEditText.getText().toString();
        String messageKey = mDatabaseReference.child("Conversations").child(convID).push().getKey();

        if (messageInput.equals("")){
            Toast.makeText(this, "Please enter you message", Toast.LENGTH_SHORT).show();
        } else{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MM dd, hh:mm:ss.SSS");
            currentTime = dateFormat.format(Calendar.getInstance().getTime());

            HashMap<String, Object> messageMap = new HashMap<>();
            mDatabaseReference.child("Conversations").child(convID).updateChildren(messageMap);

            HashMap<String, Object> messageDetail = new HashMap<>();
            messageDetail.put("Username", myUsername);
            messageDetail.put("message", messageInput);
            messageDetail.put("Time", currentTime);
            mDatabaseReference.child("Conversations").child(convID).child(messageKey).updateChildren(messageDetail);
        }

        mEditText.setText("");
    }

    private void displayMessages(DataSnapshot snapshot) {

        Iterator ite = snapshot.getChildren().iterator();

        while (ite.hasNext()){
            String timeStamp = ((DataSnapshot)ite.next()).getValue().toString();
            String userName = ((DataSnapshot)ite.next()).getValue().toString();
            String message = ((DataSnapshot)ite.next()).getValue().toString();

            mTextView.append(userName + ":\n" + message + "\n" + timeStamp + "\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
        return;
    }
}