package com.hongdacode.chatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button mButton;
    private EditText mUserName, mUserBio;
    private CircleImageView mProfileImage;

    private String mUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        loadViews();

        mAuth = FirebaseAuth.getInstance();
        mUserID = mAuth.getCurrentUser().getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateEdits();
            }
        });
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