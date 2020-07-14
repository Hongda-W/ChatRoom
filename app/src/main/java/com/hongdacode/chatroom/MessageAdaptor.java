package com.hongdacode.chatroom;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class MessageAdaptor extends RecyclerView.Adapter<MessageAdaptor.MessageViewHolder> {

    private List<Messages> mMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    public MessageAdaptor (List<Messages> mMessagesList){
        this.mMessagesList = mMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText, receiverMessageText, receiverUsername;
        public CircleImageView receiverProfileImage, senderProfileImage;
        public ImageView senderMessageImage, receiverMessageImage;

        public MessageViewHolder(@NonNull View itemView){
            super(itemView);

            senderProfileImage = itemView.findViewById(R.id.sender_profile_image);
            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            senderMessageImage = itemView.findViewById(R.id.sender_message_image);

            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            receiverUsername = itemView.findViewById(R.id.receiver_username);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverMessageImage = itemView.findViewById(R.id.receiver_message_image);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
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


        holder.receiverProfileImage.setVisibility(View.INVISIBLE);
        holder.receiverUsername.setVisibility(View.INVISIBLE);
        holder.receiverMessageText.setVisibility(View.INVISIBLE);
        holder.senderProfileImage.setVisibility(View.INVISIBLE);
        holder.senderMessageText.setVisibility(View.INVISIBLE);

        holder.senderMessageImage.getLayoutParams().height=0;
        holder.senderMessageImage.getLayoutParams().width=0;
        holder.receiverMessageImage.getLayoutParams().height=0;
        holder.receiverMessageImage.getLayoutParams().width=0;


        if (messageFromID.equals(senderID)){
            holder.senderProfileImage.setVisibility(View.VISIBLE);
            if (fromMessageType.equals("text")){
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(message);
            }else if(fromMessageType.equals("image")){
                holder.senderMessageImage.getLayoutParams().height=400;
                holder.senderMessageImage.getLayoutParams().width=400;


                Picasso.get().load(message).into(holder.senderMessageImage);
            } else if(fromMessageType.equals("docx")){
                holder.senderMessageImage.getLayoutParams().height=300;
                holder.senderMessageImage.getLayoutParams().width=300;

                Picasso.get().load("invalid").placeholder(R.drawable.docx_icon).into(holder.senderMessageImage);
            } else if(fromMessageType.equals("pdf")){
                holder.senderMessageImage.getLayoutParams().height=300;
                holder.senderMessageImage.getLayoutParams().width=300;

                Picasso.get().load("invalid").placeholder(R.drawable.pdf_icon).into(holder.senderMessageImage);
            } else if(fromMessageType.equals("xlsx")){
                holder.senderMessageImage.getLayoutParams().height=300;
                holder.senderMessageImage.getLayoutParams().width=300;

                Picasso.get().load("invalid").placeholder(R.drawable.xlsx_icon).into(holder.senderMessageImage);
            } else if(fromMessageType.equals("pptx")){
                holder.senderMessageImage.getLayoutParams().height=300;
                holder.senderMessageImage.getLayoutParams().width=300;

                Picasso.get().load("invalid").placeholder(R.drawable.pptx_icon).into(holder.senderMessageImage);
            } else if(fromMessageType.equals("audio")){
                holder.senderMessageImage.getLayoutParams().height=300;
                holder.senderMessageImage.getLayoutParams().width=300;

                Picasso.get().load("invalid").placeholder(R.drawable.audio_icon).into(holder.senderMessageImage);
            } else if(fromMessageType.equals("video")){
                holder.senderMessageImage.getLayoutParams().height=300;
                holder.senderMessageImage.getLayoutParams().width=300;

                Picasso.get().load("invalid").placeholder(R.drawable.vedio_icon).into(holder.senderMessageImage);
            } else if(fromMessageType.equals("*")){
                holder.senderMessageImage.getLayoutParams().height=300;
                holder.senderMessageImage.getLayoutParams().width=300;

                Picasso.get().load("invalid").placeholder(R.drawable.unknown_icon).into(holder.senderMessageImage);
            }
        } else{
            holder.receiverProfileImage.setVisibility(View.VISIBLE);
            if (fromMessageType.equals("text")){

                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(message);
            } else if(fromMessageType.equals("image")){
                holder.receiverMessageImage.getLayoutParams().height=400;
                holder.receiverMessageImage.getLayoutParams().width=400;

                Picasso.get().load(message).into(holder.receiverMessageImage);

            } else if(fromMessageType.equals("docx")){
                holder.receiverMessageImage.getLayoutParams().height=300;
                holder.receiverMessageImage.getLayoutParams().width=300;

                Picasso.get().load("invalid").placeholder(R.drawable.docx_icon).into(holder.receiverMessageImage);
            } else if(fromMessageType.equals("xlsx")){
                holder.receiverMessageImage.getLayoutParams().height=300;
                holder.receiverMessageImage.getLayoutParams().width=300;

                Picasso.get().load("invalid").placeholder(R.drawable.xlsx_icon).into(holder.receiverMessageImage);
            } else if(fromMessageType.equals("pdf")){
                holder.receiverMessageImage.getLayoutParams().height=300;
                holder.receiverMessageImage.getLayoutParams().width=300;

                Picasso.get().load("invalid").placeholder(R.drawable.pdf_icon).into(holder.receiverMessageImage);
            } else if(fromMessageType.equals("pptx")){
                holder.receiverMessageImage.getLayoutParams().height=300;
                holder.receiverMessageImage.getLayoutParams().width=300;

                Picasso.get().load("invalid").placeholder(R.drawable.pptx_icon).into(holder.receiverMessageImage);
            } else if(fromMessageType.equals("audio")){
                holder.receiverMessageImage.getLayoutParams().height=300;
                holder.receiverMessageImage.getLayoutParams().width=300;

                Picasso.get().load("invalid").placeholder(R.drawable.audio_icon).into(holder.receiverMessageImage);
            } else if(fromMessageType.equals("video")){
                holder.receiverMessageImage.getLayoutParams().height=300;
                holder.receiverMessageImage.getLayoutParams().width=300;

                Picasso.get().load("invalid").placeholder(R.drawable.vedio_icon).into(holder.receiverMessageImage);
            }else if(fromMessageType.equals("*")){
                holder.receiverMessageImage.getLayoutParams().height=300;
                holder.receiverMessageImage.getLayoutParams().width=300;

                Picasso.get().load("invalid").placeholder(R.drawable.unknown_icon).into(holder.receiverMessageImage);
            }

        }

    }


    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }


}
