package com.hongdacode.chatroom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button mButton;
    private EditText mUserName, mUserBio;
    private CircleImageView mProfileImage;

    private String mUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private StorageReference mImageRef;

    private ProgressDialog mProgress;

    private static final int GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        loadViews();

        mProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUserID = mAuth.getCurrentUser().getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mImageRef = FirebaseStorage.getInstance().getReference().child("ProfileImages");


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateEdits();
            }
        });
        
        getUserDetail();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult cropImageResult = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){

                mProgress.setTitle("Setting profile image");
                mProgress.setMessage("Please wait, updating your profile image...");
                mProgress.setCanceledOnTouchOutside(true);
                mProgress.show();

                Uri resultUri = cropImageResult.getUri();


                UploadTask upImageTask= mImageRef.child(mUserID+".jpg").putFile(resultUri);

                upImageTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                        if (!task.isSuccessful()){ throw task.getException(); }
                        return mImageRef.child(mUserID+".jpg").getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri imageUri = task.getResult();
                            Toast.makeText(SettingsActivity.this, "Profile image updated successfully.", Toast.LENGTH_SHORT).show();
                            if (imageUri != null){
                                String imageURL = imageUri.toString();
                                mDatabaseRef.child("Users").child(mUserID).child("profileImage")
                                        .setValue(imageURL)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Toast.makeText(SettingsActivity.this, "Profile image url saved in user database.", Toast.LENGTH_SHORT).show();
                                                } else{
                                                    showErrorDialog("Profile image url was not able to be saved in user database");
                                                }
                                                mProgress.dismiss();
                                            }
                                        });
                            }
                        } else {
                            showErrorDialog("Some error occurred when uploading your profile image.");
                            mProgress.dismiss();
                        }
                    }
                });
            }

        }
    }

    private void updateEdits() {
        String userName = mUserName.getText().toString();
        String userBio = mUserBio.getText().toString();

        if (userName.equals("")) {
            mUserName.setError("Username cannot be empty.");
        } else{
            HashMap<String, String> profileMap = new HashMap<>();
                profileMap.put("uid", mUserID);
                profileMap.put("Username", userName);
                profileMap.put("UserBio", userBio);
            mDatabaseRef.child("Users").child(mUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(SettingsActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                                sendToMainActivity();
                            } else {
                                String message = task.getException().toString();
                                showErrorDialog(message);
                            }
                        }
                    });
        }
    }

    private void loadViews() {
        mButton = findViewById(R.id.edit_update_button);
        mUserName = findViewById(R.id.edit_user_name);
        mUserBio = findViewById(R.id.edit_user_bio);
        mProfileImage = findViewById(R.id.edit_profile_image);
    }


    private void getUserDetail() {
        mDatabaseRef.child("Users").child(mUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if ((snapshot.exists()) && (snapshot.hasChild("Username")) && (snapshot.hasChild("profileImage"))) {
                            String outUserName = snapshot.child("Username").getValue().toString();
                            String outUserBio = snapshot.child("UserBio").getValue().toString();
                            String outUserImage = snapshot.child("profileImage").getValue().toString();

                            mUserName.setText(outUserName);
                            mUserBio.setText(outUserBio);

                            Picasso.get().load(outUserImage).into(mProfileImage);


                        } else if ((snapshot.exists()) && (snapshot.hasChild("Username"))) {
                            String outUserName = snapshot.child("Username").getValue().toString();
                            String outUserBio = snapshot.child("UserBio").getValue().toString();

                            mUserName.setText(outUserName);
                            mUserBio.setText(outUserBio);

                        } else {
                            showErrorDialog("Please add your account detail");

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void showErrorDialog(String message){
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}