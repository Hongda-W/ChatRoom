package com.hongdacode.chatroom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class GroupConversationActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton mSendMessageButton;
    private EditText mMessageView;
    private ScrollView mScrollView;
    private TextView mTextView;

    private String mGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_conversation);

        mGroupName = getIntent().getStringExtra("GroupName");
        Toast.makeText(GroupConversationActivity.this, mGroupName, Toast.LENGTH_SHORT).show();


        loadViews();
    }

    private void loadViews() {
        mToolbar = findViewById(R.id.group_conversation_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(mGroupName);

        mSendMessageButton = findViewById(R.id.send_group_message);
        mMessageView = findViewById(R.id.group_message_input);

        mTextView = findViewById(R.id.group_conversation_text);
        mScrollView = findViewById(R.id.group_scroll_view);


    }
}