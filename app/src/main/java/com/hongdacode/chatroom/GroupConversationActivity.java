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
import com.google.firebase.storage.UploadTask;

import java.security.acl.Group;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GroupConversationActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button mSendMessageButton;
    private ImageButton mSendImageButton, mSendFileButton;
    private EditText mMessageView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    private final List<Messages> mMessagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private GroupMessageAdaptor mMessageAdaptor;
    private RecyclerView mRecyclerView;

    private String mGroupName, mUserID, mUsername, currentTime;
    private String fileType="", url;
    private StorageTask uploadToFirebase;
    private Uri fileUri;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_conversation);

        mGroupName = getIntent().getStringExtra("GroupName");

        mMessageAdaptor = new GroupMessageAdaptor(mMessagesList);
        mRecyclerView = findViewById(R.id.group_conv_recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mMessageAdaptor);

        mAuth = FirebaseAuth.getInstance();
        mUserID = mAuth.getCurrentUser().getUid();


        loadViews();

        getUserInfo();



        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessageToDatabase();
            }
        });

        // send message when "enter" is pressed on keyboard
        mMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                sendMessageToDatabase();
                return true;
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

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GroupConversationActivity.this);
                alertBuilder.setTitle("Select File");

                alertBuilder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0){
                            fileType="pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select pdf"), 438);

                            Toast.makeText(GroupConversationActivity.this, "You should select pdf", Toast.LENGTH_SHORT).show();
                        }
                        if (i == 1){
                            fileType="docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select docx"), 438);
                        }
                        if (i == 2){
                            fileType="xlsx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/vnd.ms-excel");
                            startActivityForResult(intent.createChooser(intent, "Select xlsx"), 438);
                        }
                        if (i == 3){
                            fileType="pptx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/vnd.ms-powerpoint");
                            startActivityForResult(intent.createChooser(intent, "Select pptx"), 438);
                        }

                        if (i == 4){
                            fileType="audio";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("audio/*");
                            startActivityForResult(intent.createChooser(intent, "Select audio file"), 438);
                        }

                        if (i == 5){
                            fileType="video";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("video/*");
                            startActivityForResult(intent.createChooser(intent, "Select video file"), 438);
                        }

                        if (i == 6){
                            fileType="*";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("*/*");
                            startActivityForResult(intent.createChooser(intent, "Select any file"), 438);
                        }


                    }
                });
                alertBuilder.show();
            }
        });

        mDatabaseRef.child("Groups").child(mGroupName).addChildEventListener(new ChildEventListener() {
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


    private void getUserInfo() {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef.child("Users").child(mUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    mUsername = snapshot.child("Username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadViews() {
        mToolbar = findViewById(R.id.group_conversation_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(mGroupName);

        mSendMessageButton = findViewById(R.id.send_group_message);
        mSendFileButton = findViewById(R.id.send_group_file);
        mSendImageButton = findViewById(R.id.send_group_image);
        mMessageView = findViewById(R.id.group_message_input);

    }

    private void sendMessageToDatabase() {

        String messageInput = mMessageView.getText().toString();
        String messageKey = mDatabaseRef.child("Groups").child(mGroupName).push().getKey();

        sendChatToDatabase(messageInput, "text", messageKey);

        mMessageView.setText("");
    }

    private void sendChatToDatabase(String message, String type, String messageKey){
        if (message.equals("")){
            Toast.makeText(this, "Please enter you message", Toast.LENGTH_SHORT).show();
        } else{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MM dd, hh:mm:ss.SSS");
            currentTime = dateFormat.format(Calendar.getInstance().getTime());

            HashMap<String, Object> groupMessageMap = new HashMap<>();
            mDatabaseRef.child("Groups").child(mGroupName).updateChildren(groupMessageMap);

            HashMap<String, Object> groupMessageDetail = new HashMap<>();
            groupMessageDetail.put("UserID", mUserID);
            groupMessageDetail.put("message", message);
            groupMessageDetail.put("Time", currentTime);
            groupMessageDetail.put("type", type);
            mDatabaseRef.child("Groups").child(mGroupName).child(messageKey).updateChildren(groupMessageDetail);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            mProgress.setTitle("Sending file");
            mProgress.setMessage("Please wait, file is being sent...");
            mProgress.setCanceledOnTouchOutside(true);
            mProgress.show();

            fileUri = data.getData();

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ChatFiles");
            final StorageReference fileUploaded;

            final String messageKey = mDatabaseRef.child("Groups").child(mGroupName).push().getKey();


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

                            sendChatToDatabase(imageURL, fileType, messageKey);
                            mProgress.dismiss();
                        }
                    }
                }
            });

        }
    }
}