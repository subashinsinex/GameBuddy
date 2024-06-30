package com.GameBuddy.gb;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Chat_Fragment extends Fragment implements UsersAdapter.OnUserClickListener {

    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> userList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        progressBar = view.findViewById(R.id.progressBar);

        userList = new ArrayList<>();
        adapter = new UsersAdapter(getContext(), userList);
        adapter.setOnUserClickListener(this); // Set the listener in Fragment
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID
        fetchUsers();

        return view;
    }

    private void fetchUsers() {
        progressBar.setVisibility(View.VISIBLE);

        // Use a Set to store unique user IDs
        Set<String> userIds = new HashSet<>();

        // Query messages where the current user is sender
        db.collection("messages")
                .whereEqualTo("senderId", currentUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot senderMessages, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("ChatFragment", "Error fetching sender messages", error);
                            return;
                        }

                        // Add receivers' IDs from sender messages
                        for (QueryDocumentSnapshot document : senderMessages) {
                            String receiverId = document.getString("receiverId");
                            userIds.add(receiverId);
                        }

                        // Query messages where the current user is receiver
                        db.collection("messages")
                                .whereEqualTo("receiverId", currentUserId)
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot receiverMessages, @Nullable FirebaseFirestoreException error) {
                                        if (error != null) {
                                            Log.e("ChatFragment", "Error fetching receiver messages", error);
                                            return;
                                        }

                                        // Add senders' IDs from receiver messages
                                        for (QueryDocumentSnapshot document : receiverMessages) {
                                            String senderId = document.getString("senderId");
                                            userIds.add(senderId);
                                        }

                                        // Fetch users based on collected user IDs
                                        fetchUsersFromIds(userIds);
                                    }
                                });
                    }
                });
    }

    private void fetchUsersFromIds(Set<String> userIds) {
        // Fetch users based on collected user IDs
        db.collection("users")
                .whereIn(FieldPath.documentId(), new ArrayList<>(userIds)) // Use FieldPath.documentId() to query by document ID
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        progressBar.setVisibility(View.GONE);
                        if (error != null) {
                            Log.e("ChatFragment", "Error fetching users", error);
                            return;
                        }

                        userList.clear();
                        for (QueryDocumentSnapshot document : value) {
                            String uid = document.getId();
                            if (uid.equals(currentUserId)) {
                                continue; // Skip current user
                            }

                            // Retrieve user data
                            String username = document.getString("username");
                            String name = document.getString("name");
                            String email = document.getString("email");
                            String phone = document.getString("phone");
                            String status = document.getString("status");
                            String profile_pic = document.getString("profile_pic");

                            // Create a User object
                            User user = new User(uid, username, name, email, phone, status, profile_pic);
                            userList.add(user);
                        }

                        // Update UI
                        adapter.notifyDataSetChanged();
                    }
                });
    }


    @Override
    public void onUserClick(User user) {
        Log.d("Chat_Fragment", "User ID clicked: " + user.getUid());
        Log.d("Chat_Fragment", "User Name clicked: " + user.getUsername());

        // Pass user data to Chat_Activity
        startActivity(Chat.newIntent(getContext(), user.getUid(), user.getUsername(), user.getProfile_pic()));
    }
}
