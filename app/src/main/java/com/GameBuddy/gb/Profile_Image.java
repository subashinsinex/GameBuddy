package com.GameBuddy.gb;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.GameBuddy.gb.databinding.ActivityProfileImageBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class Profile_Image extends AppCompatActivity {

    private ActivityProfileImageBinding binding;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private final ActivityResultLauncher<String> getContentLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleImageResult
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityProfileImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        AppCompatButton get = binding.get;

        get.setOnClickListener(v -> getContentLauncher.launch("image/*"));

        loadProfilePicture();
    }

    private void loadProfilePicture() {
        String uid = currentUser.getUid();
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String profilePicUrl = document.getString("profile_pic");
                            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(profilePicUrl)
                                        .placeholder(R.drawable.baseline_person_24)
                                        .error(R.drawable.baseline_person_24)
                                        .into(binding.profileImage);
                            } else {
                                binding.profileImage.setImageResource(R.drawable.baseline_person_24);
                            }
                        } else {
                            Toast.makeText(Profile_Image.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Profile_Image.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleImageResult(Uri uri) {
        if (uri != null) {
            binding.profileImage.setImageURI(uri);
            String uid = currentUser.getUid();
            final StorageReference reference = storage.getReference().child("profile_pic").child(uid);
            reference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                reference.getDownloadUrl().addOnSuccessListener(uriDownload -> {
                    Map<String, Object> user = new HashMap<>();
                    user.put("profile_pic", uriDownload.toString());
                    db.collection("users").document(uid).update(user).addOnSuccessListener(aVoid -> {
                        Toast.makeText(Profile_Image.this, "Profile Picture Updated", Toast.LENGTH_SHORT).show();
                        // Navigate to Main class's profile fragment
                        Intent intent = new Intent(Profile_Image.this, Main.class);
                        intent.putExtra("fragmentToLoad", "profileFragment"); // Assuming the key for profile fragment
                        startActivity(intent);
                        finish();
                    });
                });
            });
        }
    }

}
