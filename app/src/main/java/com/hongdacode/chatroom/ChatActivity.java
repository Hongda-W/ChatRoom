package com.hongdacode.chatroom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button mSendMessageButton;
    private ImageButton mSendImageButton, mSendFileButton;
    private EditText mEditText;

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;

    private final List<Messages> mMessagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private MessageAdaptor mMessageAdaptor;
    private RecyclerView mRecyclerView;

    private String conversationID, myUserID, myUsername, theirUserID, currentTime;
    private String fileType="";
    private StorageTask uploadToFirebase;
    private Uri fileUri;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        loadViews();

        mMessageAdaptor = new MessageAdaptor(mMessagesList);
        mRecyclerView = findViewById(R.id.chat_recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mMessageAdaptor);


        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        myUserID = mAuth.getCurrentUser().getUid();

        theirUserID = getIntent().getStringExtra("theirUserID");

        mProgress = new ProgressDialog(this);

        getUserInfo();

        mDatabaseReference.child("ConversationIndex").child(myUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final String convID;
                if (snapshot.exists() && snapshot.hasChild(theirUserID) &&
                        snapshot.child(theirUserID).hasChild("id") ){
                    convID = snapshot.child(theirUserID).child("id").getValue().toString();
                } else{
                    createChat(myUserID, theirUserID);
                    convID = conversationID;
                    mDatabaseReference.child("ConversationIndex").child(myUserID).child(theirUserID).child("id").setValue(conversationID);
                    mDatabaseReference.child("ConversationIndex").child(theirUserID).child(myUserID).child("id").setValue(conversationID);
                    mDatabaseReference.child("Conversations").child(convID).setValue("")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(ChatActivity.this,  "Chat with ID: "+ conversationID+ "created successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

                mSendMessageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendMessageToDatabase(convID);
                    }
                });

                // send message when "enter" is pressed on keyboard
                mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        sendMessageToDatabase(convID);
                        return true;
                    }
                });

                mSendFileButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence options[] = new CharSequence[]{
                                "PDF File",
                                "DocX File",
                                "Excel File",
                                "PPT File",
                                "Audio File",
                                "Video File",
                                "Any File"
                        };

                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ChatActivity.this);
                        alertBuilder.setTitle("Select File");

                        alertBuilder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0){
                                    fileType="pdf";

                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    intent.setType("application/pdf");
                                    intent.putExtra("convID", convID);
                                    startActivityForResult(intent.createChooser(intent, "Select pdf"), 438);

                                }
                                if (i == 1){
                                    fileType="docx";
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    intent.setType("application/msword");
                                    intent.putExtra("convID", convID);
                                    startActivityForResult(intent.createChooser(intent, "Select docx"), 438);
                                }
                                if (i == 2){
                                    fileType="xlsx";
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    intent.setType("application/vnd.ms-excel");
                                    intent.putExtra("convID", convID);
                                    startActivityForResult(intent.createChooser(intent, "Select xlsx"), 438);
                                }
                                if (i == 3){
                                    fileType="pptx";
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    intent.setType("application/vnd.ms-powerpoint");
                                    intent.putExtra("convID", convID);
                                    startActivityForResult(intent.createChooser(intent, "Select pptx"), 438);
                                }

                                if (i == 4){
                                    fileType="audio";
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    intent.setType("audio/*");
                                    intent.putExtra("convID", convID);
                                    startActivityForResult(intent.createChooser(intent, "Select audio file"), 438);
                                }

                                if (i == 5){
                                    fileType="video";
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    intent.setType("video/*");
                                    intent.putExtra("convID", convID);
                                    startActivityForResult(intent.createChooser(intent, "Select video file"), 438);
                                }

                                if (i == 6){
                                    fileType="*";
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    intent.setType("*/*");
                                    intent.putExtra("convID", convID);
                                    startActivityForResult(intent.createChooser(intent, "Select any file"), 438);
                                }


                            }
                        });
                        alertBuilder.show();
                    }
                });

                mSendImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        fileType="image";
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDatabaseReference.child("Users").child(myUserID).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    final String mUsername = snapshot.child("Username").getValue().toString();

                    mDatabaseReference.child("Users").child(theirUserID).getRef().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                final String tUsername =snapshot.child("Username").getValue().toString();

                                getSupportActionBar().setTitle(tUsername);

                                Toast.makeText(ChatActivity.this, mUsername + "'s conversation with "+tUsername, Toast.LENGTH_SHORT).show();
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

        mDatabaseReference.child("ConversationIndex").child(myUserID).child(theirUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("id") ){
                    final String convID = snapshot.child("id").getValue().toString();
                    mDatabaseReference.child("Conversations").child(convID).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            Messages message = snapshot.getValue(Messages.class);

                            mMessagesList.add(message);

                            mMessageAdaptor.notifyDataSetChanged();

                            mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount());
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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

    }


    private void createChat(final String myUserID, final String theirUserID) {
        conversationID = createTransactionID();
        mDatabaseReference.child("Conversations").getRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild(conversationID)){
                    createChat(myUserID, theirUserID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public String createTransactionID(){
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    private void loadViews() {
        mToolbar = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(mToolbar);

        mSendMessageButton = findViewById(R.id.send_chat_message);
        mSendImageButton = findViewById(R.id.send_chat_image);
        mSendFileButton = findViewById(R.id.send_chat_file);
        mEditText = findViewById(R.id.chat_message_input);


    }

    private void getUserInfo() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(myUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    myUsername = snapshot.child("Username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessageToDatabase(String convID) {

        String messageInput = mEditText.getText().toString();

        String messageKey = mDatabaseReference.child("Conversations").child(convID).push().getKey();


        sendChatToDatabase(convID, messageInput, "text", messageKey);

        mEditText.setText("");
    }

    private void sendChatToDatabase(String convID, String message, String type, String messageKey) {


        if (message.equals("")){
            Toast.makeText(this, "Please enter you message", Toast.LENGTH_SHORT).show();
        } else{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MM dd, hh:mm:ss.SSS");
            currentTime = dateFormat.format(Calendar.getInstance().getTime());

            HashMap<String, Object> messageMap = new HashMap<>();
            mDatabaseReference.child("Conversations").child(convID).updateChildren(messageMap);

            HashMap<String, Object> messageDetail = new HashMap<>();
            messageDetail.put("UserID", myUserID);
            messageDetail.put("message", message);
            messageDetail.put("Time", currentTime);
            messageDetail.put("type", type);
            mDatabaseReference.child("Conversations").child(convID).child(messageKey).updateChildren(messageDetail);
        }

        mEditText.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            mProgress.setTitle("Sending file");
            mProgress.setMessage("Please wait, file is being sent...");
            mProgress.setCanceledOnTouchOutside(true);
            mProgress.show();

            mDatabaseReference.child("ConversationIndex").child(myUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    final String convID;
                    if (snapshot.exists() && snapshot.hasChild(theirUserID) &&
                            snapshot.child(theirUserID).hasChild("id") ){

                        fileUri = data.getData();

                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ChatFiles");
                        final StorageReference fileUploaded;
                        convID = snapshot.child(theirUserID).child("id").getValue().toString();
                        final String messageKey = mDatabaseReference.child("Conversations").child(convID).push().getKey();
                        fileUploaded = storageReference.child(messageKey + "_" + fileType);
                        uploadToFirebase = fileUploaded.putFile(fileUri);
                        uploadToFirebase.continueWithTask(new Continuation() {
                            @Override
                            public Object then(@NonNull Task task) throws Exception {

                                if (!task.isSuccessful()){
                                    throw task.getException();
                                }
                                return fileUploaded.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){
                                    Uri imageUri = (Uri) task.getResult();

                                    if (imageUri != null){
                                        String imageURL = imageUri.toString();

                                        sendChatToDatabase(convID, imageURL, fileType, messageKey);
                                        mProgress.dismiss();
                                    }
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }
    }

}