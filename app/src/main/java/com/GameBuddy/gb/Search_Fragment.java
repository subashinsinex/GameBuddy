package com.GameBuddy.gb;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Search_Fragment extends Fragment implements UsersAdapter.OnUserClickListener {

    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> userList;
    private List<User> filteredList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextInputEditText searchInput;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        progressBar = view.findViewById(R.id.progressBar);
        searchInput = view.findViewById(R.id.searchInput);

        userList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new UsersAdapter(getContext(), filteredList);
        adapter.setOnUserClickListener(this);// Pass 'this' as the listener
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID
        fetchUsers();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterUsers(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        return view;
    }

    private void fetchUsers() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        progressBar.setVisibility(View.GONE);
                        if (error != null) {
                            Log.e("SearchFragment", "Error fetching users", error);
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

                        // Update filtered list initially with all users except current user
                        filteredList.clear();
                        filteredList.addAll(userList);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void filterUsers(String query) {
        filteredList.clear();
        if (!query.isEmpty()) {
            for (User user : userList) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(user);
                }
            }
        } else {
            filteredList.addAll(userList); // Show all users if query is empty
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onUserClick(User user) {
        // Handle user click here
        Log.d("Search_Fragment", "User clicked: " + user.getUsername());

        // Example: Start Chat_Activity with user data
        startActivity(Chat.newIntent(getContext(), user.getUid(), user.getUsername(), user.getProfile_pic()));
    }
}
