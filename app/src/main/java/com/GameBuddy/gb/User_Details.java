package com.GameBuddy.gb;

import android.content.Intent;
import android.os.Bundle;
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

public class User_Details extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_details);

        TextInputEditText name = findViewById(R.id.name);
        TextInputEditText username = findViewById(R.id.username);
        TextInputEditText status = findViewById(R.id.status);
        TextInputEditText phone = findViewById(R.id.phone);
        AppCompatButton update = findViewById(R.id.btn_update);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        update.setOnClickListener(v -> {
            String nameTxt = Objects.requireNonNull(name.getText()).toString().trim();
            String usernameTxt = Objects.requireNonNull(username.getText()).toString().trim();
            String statusTxt = Objects.requireNonNull(status.getText()).toString().trim();
            String phoneTxt = Objects.requireNonNull(phone.getText()).toString().trim();
            try {
                String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                Map<String, Object> user = new HashMap<>();
                if(!usernameTxt.isEmpty()){
                    user.put("username", usernameTxt);
                }
                if (!nameTxt.isEmpty()) {
                    user.put("name", nameTxt);
                }
                if (!statusTxt.isEmpty()) {
                    user.put("status", statusTxt);
                }
                if (!phoneTxt.isEmpty()) {
                    user.put("phone", phoneTxt);
                }
                if (!user.isEmpty()) {
                    db.collection("users").document(uid).update(user)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(User_Details.this, "User Details Updated", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(User_Details.this, Main.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(User_Details.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(User_Details.this, "No fields to update", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(User_Details.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
