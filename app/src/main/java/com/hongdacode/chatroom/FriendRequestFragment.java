package com.hongdacode.chatroom;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestFragment extends Fragment {

    private View mFragView;
    private RecyclerView mRecyclerView;

    private DatabaseReference mDataRef;
    private FirebaseAuth mAuth;

    private String myUserID;

    private FirebaseRecyclerAdapter<Contacts, FriendRequestViewHolder> adapter;

    public FriendRequestFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mFragView = inflater.inflate(R.layout.fragment_friend_request, container, false);

        mRecyclerView = mFragView.findViewById(R.id.friend_request_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mDataRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest");

        mAuth = FirebaseAuth.getInstance();
        myUserID = mAuth.getCurrentUser().getUid();


        return mFragView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(mDataRef.child(myUserID), Contacts.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Contacts, FriendRequestViewHolder>(options) {

            @NonNull
            @Override
            public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout, parent, false);
                return new FriendRequestViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendRequestViewHolder holder, int position, @NonNull Contacts model) {
                holder.itemView.findViewById(R.id.user_accept_button).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.user_reject_button).setVisibility(View.VISIBLE);

                final String userID = getRef(position).getKey();

                DatabaseReference requestTypeRef = getRef(position).child("request").getRef();

                requestTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String requestFriend = snapshot.getValue().toString();
                            if (requestFriend.equals("received")){
                                FirebaseDatabase.getInstance().getReference().child("Users").child(userID)
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()){
                                                    if (snapshot.hasChild("profileImage")){
                                                        final String username = snapshot.child("Username").getValue().toString();
                                                        final String userBio = snapshot.child("UserBio").getValue().toString();
                                                        final String imageURL = snapshot.child("profileImage").getValue().toString();

                                                        holder.mUserName.setText(username);
                                                        holder.mUserStatus.setText(userBio);

                                                        Picasso.get().load(imageURL).into(holder.mProfileImage);

                                                    }else {
                                                        final String username = snapshot.child("Username").getValue().toString();
                                                        final String userBio = snapshot.child("UserBio").getValue().toString();

                                                        holder.mUserName.setText(username);
                                                        holder.mUserStatus.setText(userBio);

                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                            else if (requestFriend.equals("sent")){
                                Button req_button = holder.itemView.findViewById(R.id.user_accept_button);
                                req_button.setText("Request sent");
                                holder.itemView.findViewById(R.id.user_reject_button).setVisibility(View.INVISIBLE);

                                FirebaseDatabase.getInstance().getReference().child("Users").child(userID)
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()){
                                                    if (snapshot.hasChild("profileImage")){
                                                        final String username = snapshot.child("Username").getValue().toString();
                                                        final String userBio = snapshot.child("UserBio").getValue().toString();
                                                        final String imageURL = snapshot.child("profileImage").getValue().toString();

                                                        holder.mUserName.setText(username);
                                                        holder.mUserStatus.setText(userBio);

                                                        Picasso.get().load(imageURL).into(holder.mProfileImage);

                                                    }else {
                                                        final String username = snapshot.child("Username").getValue().toString();
                                                        final String userBio = snapshot.child("UserBio").getValue().toString();

                                                        holder.mUserName.setText(username);
                                                        holder.mUserStatus.setText(userBio);

                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.mUserName.setText(model.getUsername());
                holder.mUserStatus.setText(model.getUserBio());
                Picasso.get().load(model.getProfileImage()).placeholder(R.drawable.profile_default).into(holder.mProfileImage);
            }
        };

        mRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FriendRequestViewHolder extends RecyclerView.ViewHolder{
        TextView mUserName, mUserStatus;
        CircleImageView mProfileImage;
        Button mAcceptButton, mCancelButton;


        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            mUserName = itemView.findViewById(R.id.user_name);
            mUserStatus = itemView.findViewById(R.id.user_status);
            mProfileImage = itemView.findViewById(R.id.user_image);
            mAcceptButton = itemView.findViewById(R.id.user_accept_button);
            mCancelButton = itemView.findViewById(R.id.user_reject_button);
        }
    }

}