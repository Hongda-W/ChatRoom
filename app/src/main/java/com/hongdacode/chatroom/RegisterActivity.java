package com.hongdacode.chatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText mPassword, mConfirmPassword;
    private AutoCompleteTextView mUsername, mEmail;
    private TextView mAlreadyHaveAccount;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loadViews();

        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(this);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToLoginActivity();
            }
        });


        mConfirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent == null || keyEvent.getAction() != KeyEvent.ACTION_DOWN){
                    return false;
                }

                attemptRegistration();
                return true;
            }
        });


    }

    private void loadViews() {
        mEmail = findViewById(R.id.register_email);
        mUsername = findViewById(R.id.register_username);
        mPassword = findViewById(R.id.register_password);
        mConfirmPassword = findViewById(R.id.confirm_password);
        mAlreadyHaveAccount = findViewById(R.id.already_have_account);
    }

    public void register(View view) {
        attemptRegistration();
    }

    private void attemptRegistration() {
        mEmail.setError(null);
        mPassword.setError(null);

        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        String username = mUsername.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPassword.setError("Password is short than 6 characters or doesn't match");
            focusView = mPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmail.setError("This field is required");
            focusView = mEmail;
            cancel = true;
        } else if(!isEmailValid(email)) {
            mEmail.setError("This email address is invalid");
            focusView = mEmail;
            cancel = true;
        }


        if (cancel){
            focusView.requestFocus();
        } else {
            mProgress.setTitle("Registering new account");
            mProgress.setMessage("Please wait ...");
            mProgress.setCanceledOnTouchOutside(true);
            mProgress.show();


            createFirebaseUser(email, password, username, mProgress);
        }
    }

    private void createFirebaseUser(String email, String password, final String username, final ProgressDialog progress) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()){
                    showErrorDialog("Registration failed!");
                    String errorMessage = task.getException().toString();
                    Log.d("ChatRoom", errorMessage);
                    Toast.makeText(RegisterActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                } else {
                    String userID = mAuth.getCurrentUser().getUid();
                    mDatabaseRef.child("Users").child(userID).setValue("");

                    HashMap<String, String> profileMap = new HashMap<>();
                    profileMap.put("uid", userID);
                    profileMap.put("Username", username);
                    profileMap.put("UserBio", "");
                    mDatabaseRef.child("Users").child(userID).setValue(profileMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        sendToMainActivity();
                                    } else {
                                        String message = task.getException().toString();
                                        showErrorDialog(message);
                                    }
                                }
                            });

                    sendToMainActivity();
                    Toast.makeText(RegisterActivity.this, "Registration was successful!", Toast.LENGTH_SHORT).show();
                }
                progress.dismiss();
            }
        });
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        String confirmPassword = mConfirmPassword.getText().toString();
        return confirmPassword.equals(password) && password.length() >= 6;
    }

    private void sendToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
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