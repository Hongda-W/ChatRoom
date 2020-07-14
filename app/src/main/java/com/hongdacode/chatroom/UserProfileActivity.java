package com.hongdacode.chatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private String userID, myUserID, requestStatus;
    private CircleImageView mImageView;
    private TextView mUsername, mUserBio;
    private Button mSendRequestButton, mMessageButton;

    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userID = getIntent().getExtras().get("userID").toString();

        mAuth = FirebaseAuth.getInstance();
        myUserID = mAuth.getCurrentUser().getUid();

        loadViews();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        getUserProfile_detail();

        mDatabaseRef.child("Contacts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild(userID) && !userID.equals(myUserID) && snapshot.child(userID).hasChild(myUserID) &&
                        snapshot.child(userID).child(myUserID).child("isFriend").getValue().toString().equals("true")) {
                    requestStatus = "done";
                    mSendRequestButton.setText("Delete friend");

                    mSendRequestButton.setBackgroundColor(getResources().getColor(R.color.dark_pink));
                    mMessageButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDatabaseRef.child("FriendRequest").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    checkRequestStatus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mSendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptFriendRequest();
            }
        });

        mMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( requestStatus.equals("done")){
                    sendToConversation();
                } else if (requestStatus.equals("received")){
                    // the message button will be used to decline the friend request
                    declineFriendRequest();
                }
            }
        });
    }

    private void checkRequestStatus() {
        mDatabaseRef.child("FriendRequest").child(myUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild(userID)){
                            if (requestStatus == null){
                                requestStatus = snapshot.child(userID).child("request").getValue().toString();
                            }
                            if (requestStatus.equals("sent")){
                                mSendRequestButton.setText("Cancel request");
                            } else if (requestStatus.equals("received")){
                                mSendRequestButton.setText("Accept request");

                                mMessageButton.setBackgroundColor(getResources().getColor(R.color.dark_pink));
                                mMessageButton.setVisibility(View.VISIBLE);
                                mMessageButton.setText("Decline request");// use message button to reject request
                            }
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void attemptFriendRequest() {
        if ( requestStatus==null ){
            sendFriendRequest();
        }
        else if (requestStatus.equals("sent")){
            cancelFriendRequest();
        }
        else if (requestStatus.equals("received")){
            acceptFriendRequest();
        }
        else if (requestStatus.equals("done")){
            AlertDialog myAlertDialog = AskOption();
            myAlertDialog.show();
            myAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
            myAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
        }
    }

    private void getUserProfile_detail() {
        mDatabaseRef.child("Users").child(userID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if ( snapshot.exists() ){
                            String outUserName = snapshot.child("Username").getValue().toString();
                            String outUserBio = snapshot.child("UserBio").getValue().toString();
                            if ( snapshot.hasChild("profileImage") ){
                                String userImageURL = snapshot.child("profileImage").getValue().toString();
                                Picasso.get().load(userImageURL).into(mImageView);
                            }

                            mUsername.setText(outUserName);
                            mUserBio.setText(outUserBio);
                        }
                        if ( myUserID.equals(userID) ){
                            mSendRequestButton.setVisibility(View.INVISIBLE); // hide send friend request button if clicked on self
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendFriendRequest() {
        mDatabaseRef.child("FriendRequest").child(myUserID).child(userID).child("request")
                .setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mDatabaseRef.child("FriendRequest").child(userID).child(myUserID).child("request")
                                    .setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(UserProfileActivity.this, "Friend request sent.", Toast.LENGTH_SHORT).show();
                                                sendToMainActivity();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest() {
        mDatabaseRef.child("FriendRequest").child(myUserID).child(userID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mDatabaseRef.child("Contacts").child(myUserID).child(userID).child("isFriend").setValue("true");
                            mDatabaseRef.child("FriendRequest").child(userID).child(myUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                mDatabaseRef.child("Contacts").child(userID).child(myUserID).child("isFriend").setValue("true");
                                                Toast.makeText(UserProfileActivity.this, "Friend request accepted.", Toast.LENGTH_SHORT).show();
                                                sendToMainActivity();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void declineFriendRequest() {
        mDatabaseRef.child("FriendRequest").child(myUserID).child(userID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mDatabaseRef.child("Contacts").child(myUserID).child(userID).removeValue();
                            mDatabaseRef.child("FriendRequest").child(userID).child(myUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                mDatabaseRef.child("Contacts").child(userID).child(myUserID).removeValue();
                                                Toast.makeText(UserProfileActivity.this, "Friend request rejected.", Toast.LENGTH_SHORT).show();
                                                sendToMainActivity();
                                            }
                                        }
                                });
                        }
                    }
                });
    }

    private void cancelFriendRequest() {
        mDatabaseRef.child("FriendRequest").child(myUserID).child(userID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mDatabaseRef.child("Contacts").child(myUserID).child(userID).removeValue();
                            mDatabaseRef.child("FriendRequest").child(userID).child(myUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            mDatabaseRef.child("Contacts").child(userID).child(myUserID).removeValue();
                                            Toast.makeText(UserProfileActivity.this, "Friend request cancelled.", Toast.LENGTH_SHORT).show();
                                            sendToMainActivity();
                                        }
                                    });
                        }
                    }
                });
    }

    private void deleteFriend() {
        mDatabaseRef.child("FriendRequest").child(myUserID).child(userID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mDatabaseRef.child("Contacts").child(myUserID).child(userID).removeValue();
                            mDatabaseRef.child("FriendRequest").child(userID).child(myUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                mDatabaseRef.child("Contacts").child(userID).child(myUserID).removeValue();
                                                mDatabaseRef.child("ConversationIndex").child(userID).child(myUserID).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.exists()){
                                                            String convID = snapshot.child("id").getValue().toString();

                                                            mDatabaseRef.child("Conversations").child(convID).removeValue(); // remove conversations between the two.
                                                            FirebaseStorage.getInstance().getReference().child(convID+"_ChatFiles").delete();
                                                            // The firebase storage will not be deleted.
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                                mDatabaseRef.child("ConversationIndex").child(userID).child(myUserID).removeValue();
                                                mDatabaseRef.child("ConversationIndex").child(myUserID).child(userID).removeValue();
                                                Toast.makeText(UserProfileActivity.this, "Friend removed.", Toast.LENGTH_SHORT).show();
                                                sendToMainActivity();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }



    private void loadViews() {
        mImageView = findViewById(R.id.friend_profile_image);
        mUsername = findViewById(R.id.friend_profile_username);
        mUserBio = findViewById(R.id.friend_profile_bio);
        mSendRequestButton = findViewById(R.id.send_friend_request);
        mMessageButton = findViewById(R.id.send_message_and);
    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(UserProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendToConversation() {
        Intent chatIntent = new Intent(UserProfileActivity.this, ChatActivity.class);
        chatIntent.putExtra("theirUserID", userID);
        startActivity(chatIntent);
        finish();
    }

    private AlertDialog AskOption() {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                // set message, title, and icon
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this contact ?")
                .setIcon(android.R.drawable.ic_delete)

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteFriend();
                        dialog.dismiss();
                    }

                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .create();

        return myQuittingDialogBox;
    }
}