package com.hongdacode.chatroom;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class UserProfileActivity extends AppCompatActivity {

    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userID = getIntent().getExtras().get("userID").toString();

        Log.d("ChartRoom", userID);
    }
}