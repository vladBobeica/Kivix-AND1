package com.kivix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

public class DriverAuthorizationActivity extends AppCompatActivity {

    Button signinBtn, signupBtn;
    EditText emailEdit, passwordEdit;
    FirebaseAuth auth;
    DatabaseReference driverDatabaseRef;
    String onlineDriverID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_authorization);
        signinBtn = (Button) findViewById(R.id.signinDriver);
        signupBtn = (Button) findViewById(R.id.signupDriver);
        emailEdit = (EditText) findViewById(R.id.driverEmail);
        passwordEdit = (EditText) findViewById(R.id.driverPassword);
        auth = FirebaseAuth.getInstance();

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                RegisterDriver(password, email);
            }
        });

        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEdit.getText().toString();
                String password = passwordEdit.getText().toString();

                LoginDriver(password, email);
            }
        });

    }



    private void RegisterDriver(String password, String email) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    driverDatabaseRef = FirebaseDatabase.getInstance("https://kivix-8a820-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Drivers").child(onlineDriverID);
                    onlineDriverID = auth.getCurrentUser().getUid();
                    driverDatabaseRef.setValue(true);
                    Toast.makeText(DriverAuthorizationActivity.this, "Registration completed", Toast.LENGTH_SHORT).show();
                    Intent driverIntent = new Intent(DriverAuthorizationActivity.this, DriverMapActivity.class);
                    startActivity(driverIntent);
                }
                else {
                    Toast.makeText(DriverAuthorizationActivity.this, "Error 404", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void LoginDriver(String password, String email) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(DriverAuthorizationActivity.this, "Driver Login completed.", Toast.LENGTH_SHORT).show();
                    Intent driverIntent = new Intent(DriverAuthorizationActivity.this, DriverMapActivity.class);
                    startActivity(driverIntent);
                }
                else {
                    Toast.makeText(DriverAuthorizationActivity.this, "Error 404", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }
}