package com.hongdacode.chatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Button mLoginButton;
    private EditText mEmail, mPassword;
    private TextView mForgotPassword, mNewAccount;

    private ProgressDialog mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        loadViews();

        mNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToRegisterActivity();
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLoginUser();
            }
        });

        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent == null || keyEvent.getAction() != KeyEvent.ACTION_DOWN){
                    return false;
                }
                attemptLoginUser();
                return true;
            }
        });
    }



    private void attemptLoginUser() {
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        if (email.equals("") ){
            mEmail.setError("This field is required");
            return;
        }
        if (password.equals("") ){
            mPassword.setError("This field is required");
            return;
        }

        mProgress.setTitle("Logging in");
        mProgress.setMessage("Please wait ...");
        mProgress.setCanceledOnTouchOutside(true);
        mProgress.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    finish();
                    sendToMainActivity();
                    Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                } else{
                    showErrorDialog("There is problem signing in.");
                }
                mProgress.dismiss();
            }
        });

    }


    private void loadViews() {
        mLoginButton = findViewById(R.id.login_button);
        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        mForgotPassword = findViewById(R.id.forget_password_link);
        mNewAccount = findViewById(R.id.new_account_link);

        mAuth = FirebaseAuth.getInstance();

        mProgress = new ProgressDialog(this);
    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }

    private void sendToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
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