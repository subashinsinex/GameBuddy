package com.GameBuddy.gb;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.GameBuddy.gb.databinding.FragmentProfilePicBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class Profile_Pic extends Fragment {

    private FragmentProfilePicBinding binding;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private boolean isProcessingImage = false;

    private final ActivityResultLauncher<String> getContentLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleImageResult
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfilePicBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        storage = FirebaseStorage.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        binding.get.setOnClickListener(v -> getContentLauncher.launch("image/*"));

        loadProfilePicture();
    }

    private void loadProfilePicture() {
        String uid = currentUser.getUid();
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
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
                            Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error fetching user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleImageResult(Uri uri) {
        if (uri != null) {
            isProcessingImage = true;
            binding.get.setEnabled(false); // Disable the button during image processing
            binding.profileImage.setImageURI(uri);
            String uid = currentUser.getUid();
            final StorageReference reference = storage.getReference().child("profile_pic").child(uid);
            reference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                reference.getDownloadUrl().addOnSuccessListener(uriDownload -> {
                    Map<String, Object> user = new HashMap<>();
                    user.put("profile_pic", uriDownload.toString());
                    db.collection("users").document(uid).update(user).addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Profile Picture Updated", Toast.LENGTH_SHORT).show();
                        // Navigate back to user profile fragment
                        navigateBackToUserProfile();
                        isProcessingImage = false;
                        binding.get.setEnabled(true); // Enable the button after image processing is complete
                    });
                });
            });
        }
    }

    private void navigateBackToUserProfile() {
        Main mainActivity = (Main) requireActivity();
        mainActivity.replaceFragment(new Profile_Fragment());
    }
}
