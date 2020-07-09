package com.hongdacode.chatroom;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button mButton;
    private EditText mUserName, mUserStatus;
    private CircleImageView mProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        loadViews();
    }

    private void loadViews() {
        mButton = findViewById(R.id.edit_update_button);
        mUserName = findViewById(R.id.edit_user_name);
        mUserStatus = findViewById(R.id.edit_user_status);
        mProfileImage = findViewById(R.id.edit_profile_image);
    }
}