package com.GameBuddy.gb;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class Login extends AppCompatActivity {

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        TextInputEditText email = findViewById(R.id.email);
        TextInputEditText pass = findViewById(R.id.password);
        AppCompatButton login = findViewById(R.id.btn_login);
        TextView register = findViewById(R.id.registerNow);

        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(Login.this, Main.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailTxt = Objects.requireNonNull(email.getText()).toString().trim();
                String passTxt = Objects.requireNonNull(pass.getText()).toString().trim();
                if(emailTxt.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Enter Email Address",Toast.LENGTH_SHORT).show();
                } else if (passTxt.isEmpty()) {
                    Toast.makeText(getApplicationContext(),"Enter Password",Toast.LENGTH_SHORT).show();
                } else{
                    auth.signInWithEmailAndPassword(emailTxt, passTxt).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Login.this, Main.class);
                                startActivity(intent);
                                overridePendingTransition(0, 0);
                                finish();
                            } else {
                                Toast.makeText(Login.this, "Login Failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }
}
