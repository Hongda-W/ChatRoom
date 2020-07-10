package com.hongdacode.chatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ServiceConfigurationError;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewFriendActivity extends AppCompatActivity {

    private Toolbar mBarLayout;
    private RecyclerView mRecyclerView;

    private DatabaseReference mUsersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);

        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


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

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(mUsersRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, SearchFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, SearchFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull SearchFriendViewHolder holder, int position, @NonNull Contacts model) {
                        Log.d("ChatRoom", model.getUsername() + " - " + model.getUserBio());
                        holder.mUserName.setText(model.getUsername());
                        holder.mUserStatus.setText(model.getUserBio());
                        Picasso.get().load(model.getProfileImage()).placeholder(R.drawable
                        .profile_default).into(holder.mProfileImage);
                    }

                    @NonNull
                    @Override
                    public SearchFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout, parent, false);
                        return new SearchFriendViewHolder(view);
                    }
                };

        mRecyclerView.setAdapter(adapter);

        adapter.startListening();
    }

    public static class SearchFriendViewHolder extends RecyclerView.ViewHolder{


        TextView mUserName, mUserStatus;
        CircleImageView mProfileImage;


        public SearchFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            mUserName = itemView.findViewById(R.id.user_name);
            mUserStatus = itemView.findViewById(R.id.user_status);
            mProfileImage = itemView.findViewById(R.id.user_image);
        }
    }
}