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

public class CustomerAuthorizationActivity extends AppCompatActivity {

    Button signinBtn, signupBtn;
    EditText emailEdit, passwordEdit;
    FirebaseAuth customerAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_authorization);

        customerAuth = FirebaseAuth.getInstance();
        signinBtn = (Button) findViewById(R.id.signincustomer);
        signupBtn = (Button) findViewById(R.id.signupcustomer);
        emailEdit = (EditText) findViewById(R.id.customerEmail);
        passwordEdit = (EditText) findViewById(R.id.customerPassword);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEdit.getText().toString();
                String password = passwordEdit.getText().toString();

                RegisterCustomer(password, email);
            }
        });

        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEdit.getText().toString();
                String password = passwordEdit.getText().toString();

                LoginCustomer(password, email);
            }
        });

    }

    private void LoginCustomer(String password, String email) {
        customerAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CustomerAuthorizationActivity.this, "Customer Login completed", Toast.LENGTH_SHORT).show();

                    Intent customerIntent = new Intent(CustomerAuthorizationActivity.this, CustomersMapAcitvity.class);
                    startActivity(customerIntent);
                }
                else {
                    Toast.makeText(CustomerAuthorizationActivity.this, "Error 404", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void RegisterCustomer(String password, String email ) {
            customerAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CustomerAuthorizationActivity.this, "Registration completed", Toast.LENGTH_SHORT).show();
                        Intent customerIntent = new Intent(CustomerAuthorizationActivity.this, CustomersMapAcitvity.class);
                        startActivity(customerIntent);
                    }
                    else {
                        Toast.makeText(CustomerAuthorizationActivity.this, "Error 404", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }