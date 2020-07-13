package com.hongdacode.chatroom;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessageAdaptor extends RecyclerView.Adapter<GroupMessageAdaptor.GroupMessageViewHolder> {

    private List<Messages> mMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    public GroupMessageAdaptor (List<Messages> mMessagesList){
        this.mMessagesList = mMessagesList;
    }

    public class GroupMessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText, receiverMessageText, receiverUsername;
        public CircleImageView receiverProfileImage, senderProfileImage;

        public GroupMessageViewHolder(@NonNull View itemView){
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverUsername = itemView.findViewById(R.id.receiver_username);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            senderProfileImage = itemView.findViewById(R.id.sender_profile_image);
        }
    }


    @NonNull
    @Override
    public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new GroupMessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final GroupMessageViewHolder holder, int position) {
        String senderID = mAuth.getCurrentUser().getUid();
        Messages messages = mMessagesList.get(position);

        String messageFromID = messages.getUserID();
        String fromMessageType = messages.getType();
        String message = messages.getMessage();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mDatabaseReference.child(messageFromID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("profileImage")){
                    String imageURL = snapshot.child("profileImage").getValue().toString();

                    Picasso.get().load(imageURL).placeholder(R.drawable.profile_default).into(holder.receiverProfileImage);
                }
                if (snapshot.hasChild("Username")){
                    String username = snapshot.child("Username").getValue().toString();
                    holder.receiverUsername.setText(username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDatabaseReference.child(senderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("profileImage")){
                    String imageURL = snapshot.child("profileImage").getValue().toString();

                    Picasso.get().load(imageURL).placeholder(R.drawable.profile_default).into(holder.senderProfileImage
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (fromMessageType.equals("text")){
            holder.receiverMessageText.setVisibility(View.INVISIBLE);
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
            holder.receiverUsername.setVisibility(View.INVISIBLE);
            holder.senderMessageText.setVisibility(View.INVISIBLE);
            holder.senderProfileImage.setVisibility(View.INVISIBLE);

            if (messageFromID.equals(senderID)){
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderProfileImage.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(message);

            } else{
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverUsername.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(message);
            }
        }

    }


    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }


}
