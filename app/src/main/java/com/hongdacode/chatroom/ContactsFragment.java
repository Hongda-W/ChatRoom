package com.hongdacode.chatroom;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Layout;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private View mFragView;


    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    private String myUserID;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mFragView = inflater.inflate(R.layout.fragment_contacts, container, false);

        mRecyclerView = mFragView.findViewById(R.id.contacts_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();
        myUserID = mAuth.getCurrentUser().getUid();

        return mFragView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(mDatabaseRef.child(myUserID), Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> (options){
            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout, parent, false);
                return new ContactsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {
                final String userID = getRef(position).getKey();
                DatabaseReference isFriendRef = getRef(position).child("isFriend").getRef();
                isFriendRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            final String isFriend = snapshot.getValue().toString();
                            if (isFriend.equals("true")){
                                FirebaseDatabase.getInstance().getReference().child("Users").child(userID)
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent userProfileIntent = new Intent(getContext(), UserProfileActivity.class);
                        userProfileIntent.putExtra("userID", userID);
                        startActivity(userProfileIntent);
                    }
                });
            }
        };

        mRecyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{
        TextView mUserName, mUserStatus;
        CircleImageView mProfileImage;


        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            mUserName = itemView.findViewById(R.id.user_name);
            mUserStatus = itemView.findViewById(R.id.user_status);
            mProfileImage = itemView.findViewById(R.id.user_image);
        }
    }
}