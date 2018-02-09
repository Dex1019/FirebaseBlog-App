package com.example.dex.firebaseblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";

    private EditText mNameField;
    private EditText mPasswordField;
    private EditText mEmailField;
    private Button mSignUpBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mNameField = findViewById(R.id.et_sign_up_name);
        mPasswordField = findViewById(R.id.et_sign_up_passsword);
        mEmailField = findViewById(R.id.et_sign_up_email);
        mSignUpBtn = findViewById(R.id.btn_signup);

        progressDialog = new ProgressDialog(this);

        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegister();
            }
        });
    }

    private void startRegister() {

        final String name = mNameField.getText().toString().trim();
        String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    progressDialog.setMessage("Signing Up...");
                    progressDialog.show();

                    if (task.isSuccessful()) {
                        try {
                            Log.d(TAG, "startRegister: called");
                            String user_id = mAuth.getCurrentUser().getUid();

                            DatabaseReference current_user_db = mDatabase.child(user_id);

                            current_user_db.child("name").setValue(name);
                            current_user_db.child("image").setValue("default");

                            progressDialog.dismiss();
                            Toast.makeText(SignUpActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                            Intent mainIntent = new Intent(SignUpActivity.this, SignInActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainIntent);

                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }


                }
            });
        }


    }
}
