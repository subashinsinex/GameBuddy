package com.GameBuddy.gb;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile_Fragment extends Fragment {

    private TextView profileName , profileUsername , profileStatus , profileEmail , profilePhone;
    private ImageView photo;
    private Button editProfile, logout;

    public Profile_Fragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String username = document.getString("username");
                                String name = document.getString("name");
                                String status = document.getString("status");
                                String email = document.getString("email");
                                String phone = document.getString("phone");
                                profileUsername.setText(username);
                                profileName.setText(name);
                                profileStatus.setText(status);
                                profileEmail.setText(email);
                                profilePhone.setText(phone);
                            } else {
                                Toast.makeText(getActivity(), "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Error fetching user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        photo = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileUsername = view.findViewById(R.id.profile_username);
        profileStatus = view.findViewById(R.id.profile_status);
        profileEmail = view.findViewById(R.id.profile_email);
        profilePhone = view.findViewById(R.id.profile_phone);
        logout = view.findViewById(R.id.logout);
        editProfile = view.findViewById(R.id.edit_profile);

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), Login.class));
            requireActivity().finish();
        });

        editProfile.setOnClickListener(v -> {
            Intent  intent = new Intent(getActivity(), User_Details.class);
            startActivity(intent);
        });

        photo.setOnClickListener(v -> {
            Intent intent =new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,25);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
