package com.hongdacode.chatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ServiceConfigurationError;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewFriendActivity extends AppCompatActivity {

    private Toolbar mBarLayout;
    private RecyclerView mRecyclerView;

    private DatabaseReference mUsersRef;

    private FirebaseAuth mAuth;

    private String myUserID;

    private FirebaseRecyclerAdapter<Contacts, SearchFriendViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);

        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        myUserID = mAuth.getCurrentUser().getUid();


        mBarLayout = findViewById(R.id.new_friend_bar_layout);
        mRecyclerView = findViewById(R.id.new_friend_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setSupportActionBar(mBarLayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Search for friend");

    }

    @Override
    protected void onStart() {
        super.onStart();

        Query query = mUsersRef.orderByChild("Username");

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(query, Contacts.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Contacts, SearchFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SearchFriendViewHolder holder, final int position, @NonNull Contacts model) {

                final String userID = getRef(position).getKey();
                final Button acceptButton = holder.mAcceptButton;
                final Button rejectButton = holder.mRejectButton;

                holder.mUserName.setText(model.getUsername());
                holder.mUserStatus.setText(model.getUserBio());
                Picasso.get().load(model.getProfileImage()).placeholder(R.drawable.profile_default).into(holder.mProfileImage);

                FirebaseDatabase.getInstance().getReference().child("FriendRequest").child(myUserID)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.hasChild(userID)){
                                    acceptButton.setVisibility(View.VISIBLE);
                                    rejectButton.setVisibility(View.VISIBLE);
                                    final String friendRequest = snapshot.child(userID).child("request").getValue().toString();
                                    if (friendRequest.equals("sent")){
                                        acceptButton.setText("Request sent");
                                        rejectButton.setVisibility(View.INVISIBLE);
                                    } else if (friendRequest.equals("received")) {
                                        Log.d("ChatRoom", "accept friend request button pressed");
                                        final DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
                                        acceptButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
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
                                                                                        Toast.makeText(NewFriendActivity.this, "Friend request accepted.", Toast.LENGTH_SHORT).show();
                                                                                        Intent intent = getIntent();
                                                                                        finish();
                                                                                        startActivity(intent);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        });

                                        rejectButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
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
                                                                                        Toast.makeText(NewFriendActivity.this, "Friend request rejected.", Toast.LENGTH_SHORT).show();
                                                                                        Intent intent = getIntent();
                                                                                        finish();
                                                                                        startActivity(intent);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent userProfileIntent = new Intent(NewFriendActivity.this, UserProfileActivity.class);
                        userProfileIntent.putExtra("userID", userID);
                        startActivity(userProfileIntent);

                    }
                });
            }

            @NonNull
            @Override
            public SearchFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout, parent, false);
                return new SearchFriendViewHolder(view);
            }
        };

        adapter.startListening();
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public static class SearchFriendViewHolder extends RecyclerView.ViewHolder{


        TextView mUserName, mUserStatus;
        CircleImageView mProfileImage;
        Button mAcceptButton, mRejectButton;


        public SearchFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            mUserName = itemView.findViewById(R.id.user_name);
            mUserStatus = itemView.findViewById(R.id.user_status);
            mProfileImage = itemView.findViewById(R.id.user_image);
            mAcceptButton = itemView.findViewById(R.id.user_accept_button);
            mRejectButton = itemView.findViewById(R.id.user_reject_button);
        }
    }
}