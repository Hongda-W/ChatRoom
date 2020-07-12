package com.hongdacode.chatroom;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private View mFragView;


    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef, mChatIndexRef;

    private String myUserID;


    public ConversationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mFragView = inflater.inflate(R.layout.fragment_conversations, container, false);

        mRecyclerView = mFragView.findViewById(R.id.conversations_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mChatIndexRef = mDatabaseRef.child("ConversationIndex");

        mAuth = FirebaseAuth.getInstance();
        myUserID = mAuth.getCurrentUser().getUid();

        return mFragView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(mChatIndexRef.child(myUserID), Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> (options){

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout, parent, false);
                return new ChatsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                final String theirUserID = getRef(position).getKey();
                DatabaseReference chatIDRef = getRef(position).child("id").getRef();

                chatIDRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            mDatabaseRef.child("Users").child(theirUserID)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild("profileImage")){
                                                final String username = snapshot.child("Username").getValue().toString();
                                                final String userBio = snapshot.child("UserBio").getValue().toString();
                                                final String imageURL = snapshot.child("profileImage").getValue().toString();

                                                holder.mUserName.setText(username);
                                                holder.mUserStatus.setText(userBio);

                                                Picasso.get().load(imageURL).into(holder.mProfileImage);
                                            } else {
                                                final String username = snapshot.child("Username").getValue().toString();
                                                final String userBio = snapshot.child("UserBio").getValue().toString();

                                                holder.mUserName.setText(username);
                                                holder.mUserStatus.setText(userBio);
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

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                        chatIntent.putExtra("theirUserID", theirUserID);
                        startActivity(chatIntent);
                    }
                });

            }
        };

        mRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{
        TextView mUserName, mUserStatus;
        CircleImageView mProfileImage;


        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            mUserName = itemView.findViewById(R.id.user_name);
            mUserStatus = itemView.findViewById(R.id.user_status);
            mProfileImage = itemView.findViewById(R.id.user_image);
        }
    }
}