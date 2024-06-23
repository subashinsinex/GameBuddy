package com.GameBuddy.gb;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Register extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        TextInputEditText email = findViewById(R.id.email);
        TextInputEditText pass = findViewById(R.id.password);
        TextInputEditText cpass = findViewById(R.id.c_password);
        AppCompatButton register = findViewById(R.id.btn_register);
        TextView login = findViewById(R.id.loginNow);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        register.setOnClickListener(v -> {
            String emailTxt = Objects.requireNonNull(email.getText()).toString().trim();
            String passTxt = Objects.requireNonNull(pass.getText()).toString().trim();
            String cpassTxt = Objects.requireNonNull(cpass.getText()).toString().trim();

            if (emailTxt.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter Email Address", Toast.LENGTH_SHORT).show();
            } else if (passTxt.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter Password", Toast.LENGTH_SHORT).show();
            } else if (!passTxt.equals(cpassTxt)) {
                Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                auth.createUserWithEmailAndPassword(emailTxt, passTxt).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", emailTxt);

                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(Register.this, "User Registered", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Register.this, User_Details.class);
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(Register.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        login.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }
}
