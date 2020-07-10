package com.hongdacode.chatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private String userID, myUserID, requestStatus;
    private CircleImageView mImageView;
    private TextView mUsername, mUserBio;
    private Button mSendRequestButton;

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

        mDatabaseRef.child("FriendRequest").child(myUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if ( snapshot.hasChild(userID)){
                                requestStatus = snapshot.child(userID).child("request").getValue().toString();
                                if (requestStatus.equals("sent")){
                                    mSendRequestButton.setText("Cancel friend request");
                                } else if (requestStatus.equals("cancelled")){
                                    mSendRequestButton.setText("Send request");
                                }
                            } else {
                                requestStatus = "no request";
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        mSendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFriendRequest();
            }
        });
    }

    private void sendFriendRequest() {
        if (requestStatus.equals("no request") || requestStatus.equals("cancelled")){
            mDatabaseRef.child("FriendRequest").child(myUserID).child(userID).child("request")
                    .setValue("sent")
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
        if (requestStatus.equals("sent")){
            mDatabaseRef.child("FriendRequest").child(myUserID).child(userID).child("request")
                    .setValue("cancelled")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(UserProfileActivity.this, "Friend request cancelled.", Toast.LENGTH_SHORT).show();
                                sendToMainActivity();
                            }
                        }
                    });
        }
    }

    private void loadViews() {
        mImageView = findViewById(R.id.friend_profile_image);
        mUsername = findViewById(R.id.friend_profile_username);
        mUserBio = findViewById(R.id.friend_profile_bio);
        mSendRequestButton = findViewById(R.id.send_friend_request);
    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(UserProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}