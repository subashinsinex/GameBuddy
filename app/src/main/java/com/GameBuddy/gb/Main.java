package com.GameBuddy.gb;

import android.content.Intent;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.GameBuddy.gb.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class Main extends AppCompatActivity {

    private User userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        com.GameBuddy.gb.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            fetchUserData(currentUser.getUid()); // Pass the UID to fetchUserData
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.chat) {
                replaceFragment(new Chat_Fragment(userData)); // Pass user data to fragments
            } else if (item.getItemId() == R.id.camera) {
                replaceFragment(new Camera_Fragment()); // Pass user data to fragments
            } else if (item.getItemId() == R.id.map) {
                replaceFragment(new Map_Fragment()); // Pass user data to fragments
            } else if (item.getItemId() == R.id.profile) {
                replaceFragment(new Profile_Fragment()); // Pass user data to fragments
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void fetchUserData(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Parse the user data from the document snapshot
                            String username = document.getString("username");
                            String email = document.getString("email");
                            String phone = document.getString("phone");
                            String status = document.getString("status");
                            // Assuming you have more fields to fetch

                            // Create a User object with the fetched data
                            userData = new User(username, email, phone,status);
                        } else {
                            Toast.makeText(Main.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Main.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
