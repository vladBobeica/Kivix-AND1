package com.kivix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverAuthorizationActivity extends AppCompatActivity {

    TextView driverStatus, question;
    Button signInBtn, signUpBtn;
    EditText emailET, passwordET;

    FirebaseAuth mAuth;
    DatabaseReference DriverDatabaseRef;
    String OnlineDriverID;

    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_authorization);
        signInBtn = (Button) findViewById(R.id.signinDriver);
        signUpBtn = (Button) findViewById(R.id.signupDriver);
        emailET = (EditText) findViewById(R.id.driverEmail);
        passwordET = (EditText) findViewById(R.id.driverPassword);
        mAuth = FirebaseAuth.getInstance();



        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();
                RegisterDriver(email, password);
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                SignInDriver(email, password);
            }
        });

    }

    private void SignInDriver(String email, String password)
    {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(DriverAuthorizationActivity.this, "Driver Login completed.", Toast.LENGTH_SHORT).show();
                    Intent driverIntent = new Intent(DriverAuthorizationActivity.this, DriverMapActivity.class);
                    startActivity(driverIntent);
                }
                else
                {
                    Toast.makeText(DriverAuthorizationActivity.this, "Error 404", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void RegisterDriver(String email, String password)
    {


        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    OnlineDriverID = mAuth.getCurrentUser().getUid();
                    DriverDatabaseRef = FirebaseDatabase.getInstance("https://kivix-8a820-default-rtdb.europe-west1.firebasedatabase.app/").getReference()
                            .child("Users").child("Drivers").child(OnlineDriverID);
                    DriverDatabaseRef.setValue(true);

                    Intent driverIntent = new Intent(DriverAuthorizationActivity.this, DriverMapActivity.class);
                    startActivity(driverIntent);

                    Toast.makeText(DriverAuthorizationActivity.this, "Registration completed", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    Toast.makeText(DriverAuthorizationActivity.this, "Error 404", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
