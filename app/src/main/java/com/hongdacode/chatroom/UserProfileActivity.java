package com.hongdacode.chatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private String userID;
    private CircleImageView mImageView;
    private TextView mUsername, mUserBio;
    private Button mSendRequestButton;

    private DatabaseReference mDatabaseRef;
    private FirebaseStorage mImageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userID = getIntent().getExtras().get("userID").toString();

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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        mSendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFriendRequest(userID);
            }
        });
    }

    private void sendFriendRequest(String identity) {
        Toast.makeText(UserProfileActivity.this, "send friend request to " + identity, Toast.LENGTH_LONG).show();
    }

    private void loadViews() {
        mImageView = findViewById(R.id.friend_profile_image);
        mUsername = findViewById(R.id.friend_profile_username);
        mUserBio = findViewById(R.id.friend_profile_bio);
        mSendRequestButton = findViewById(R.id.send_friend_request);
    }
}