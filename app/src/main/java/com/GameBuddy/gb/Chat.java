package com.GameBuddy.gb;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Chat extends AppCompatActivity {

    private static final String EXTRA_USER_ID = "extra_user_id";
    private static final String EXTRA_USER_NAME = "extra_user_name";
    private static final String EXTRA_PROFILE_PIC_URL = "extra_profile_pic_url";

    private static final String TAG = "ChatActivity";

    private ImageView toolbarProfilePic;
    private TextView toolbarUser;
    private EditText editTextMessage;
    private RecyclerView recyclerViewMessages;
    private LinearLayout layoutRoot;

    private MessageAdapter adapter;
    private List<Message> messageList;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference messageRef = db.collection("messages");

    private String currentUserId; // Current user's ID from Firebase Authentication
    private String userId; // Receiver's ID

    public static Intent newIntent(Context context, String uid, String userName, String profilePicUrl) {
        Intent intent = new Intent(context, Chat.class);
        intent.putExtra(EXTRA_USER_ID, uid);
        intent.putExtra(EXTRA_USER_NAME, userName);
        intent.putExtra(EXTRA_PROFILE_PIC_URL, profilePicUrl);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        toolbarProfilePic = findViewById(R.id.toolbar_profile_pic);
        toolbarUser = findViewById(R.id.toolbar_user);
        editTextMessage = findViewById(R.id.editTextMessage);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        layoutRoot = findViewById(R.id.main);

        userId = getIntent().getStringExtra(EXTRA_USER_ID);
        String userName = getIntent().getStringExtra(EXTRA_USER_NAME);
        String profilePicUrl = getIntent().getStringExtra(EXTRA_PROFILE_PIC_URL);

        updateToolbar(userName, profilePicUrl);

        Log.d(TAG, "User ID: " + userId);
        Log.d(TAG, "User Name: " + userName);

        // Initialize the message list and adapter
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList, currentUserId); // Pass currentUserId
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(adapter);

        // Add a Global Layout Listener to handle keyboard visibility
        layoutRoot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                layoutRoot.getWindowVisibleDisplayFrame(r);
                int screenHeight = layoutRoot.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                // Check if the keyboard is visible
                if (keypadHeight > screenHeight * 0.15) { // 15% of the screen height
                    // Keyboard is opened
                    if (adapter != null && adapter.getItemCount() > 0) {
                        recyclerViewMessages.post(new Runnable() {
                            @Override
                            public void run() {
                                recyclerViewMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                            }
                        });
                    }
                }
            }
        });

        // Handle send button click
        ImageButton buttonSend = findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle send button click
                String messageContent = editTextMessage.getText().toString().trim();
                if (!messageContent.isEmpty()) {
                    // Create a new Message object with sender and receiver IDs and timestamp
                    Message message = new Message(messageContent, currentUserId, userId, new Date());

                    // Save the message to Firestore
                    saveMessageToFirestore(message);

                    // Clear the input field
                    editTextMessage.setText("");
                }
            }
        });

        // Fetch and listen to messages
        fetchMessages();
    }

    private void updateToolbar(String userName, String profilePicUrl) {
        toolbarUser.setText(userName);
        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            loadProfileImage(profilePicUrl);
        } else {
            toolbarProfilePic.setImageResource(R.drawable.baseline_person_24);
        }
    }

    private void loadProfileImage(String profilePicUrl) {
        Glide.with(this)
                .load(profilePicUrl)
                .placeholder(R.drawable.baseline_person_24)
                .error(R.drawable.baseline_person_24)
                .circleCrop()
                .into(toolbarProfilePic);
    }

    private void saveMessageToFirestore(Message message) {
        messageRef.add(message)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Message added with ID: " + documentReference.getId());
                        // Optionally update UI or notify user
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding message", e);
                        // Handle errors
                    }
                });
    }

    private void fetchMessages() {
        messageRef
                .whereEqualTo("senderId", currentUserId)
                .whereEqualTo("receiverId", userId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Message message = dc.getDocument().toObject(Message.class);
                                    messageList.add(message);
                                    break;
                                case MODIFIED:
                                    // Handle message modification if needed
                                    break;
                                case REMOVED:
                                    // Handle message removal if needed
                                    break;
                            }
                        }

                        // Notify adapter about data changes
                        adapter.notifyDataSetChanged();
                        // Scroll to the bottom when a new message is added
                        recyclerViewMessages.post(new Runnable() {
                            @Override
                            public void run() {
                                if (adapter.getItemCount() > 0) {
                                    recyclerViewMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                                }
                            }
                        });
                    }
                });

        messageRef
                .whereEqualTo("senderId", userId)
                .whereEqualTo("receiverId", currentUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Message message = dc.getDocument().toObject(Message.class);
                                    messageList.add(message);
                                    break;
                                case MODIFIED:
                                    // Handle message modification if needed
                                    break;
                                case REMOVED:
                                    // Handle message removal if needed
                                    break;
                            }
                        }

                        // Notify adapter about data changes
                        adapter.notifyDataSetChanged();
                        // Scroll to the bottom when a new message is added
                        recyclerViewMessages.post(new Runnable() {
                            @Override
                            public void run() {
                                if (adapter.getItemCount() > 0) {
                                    recyclerViewMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                                }
                            }
                        });
                    }
                });
    }
}
