package com.GameBuddy.gb;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
//noinspection ExifInterface
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Chat extends AppCompatActivity {

    private static final String EXTRA_USER_ID = "extra_user_id";
    private static final String EXTRA_USER_NAME = "extra_user_name";
    private static final String EXTRA_PROFILE_PIC_URL = "extra_profile_pic_url";

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private static final String TAG = "ChatActivity";

    private ImageView toolbarProfilePic;
    private TextView toolbarUser;
    ImageView download;
    private EditText editTextMessage;
    private RecyclerView recyclerViewMessages;
    private LinearLayout layoutRoot;

    private MessageAdapter adapter;
    private List<Message> messageList;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference messageRef = db.collection("messages");

    private String currentUserId; // Current user's ID from Firebase Authentication
    private String userId; // Receiver's ID

    private static final String CHANNEL_ID = "message_channel";

    private String currentPhotoPath; // Path to store the captured photo

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
        download = findViewById(R.id.download);
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

        ImageButton buttonCamera = findViewById(R.id.cameraButton);
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        download.setOnClickListener(v -> { saveMessagesToTextFile();});
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
                .whereIn("senderId", Arrays.asList(currentUserId, userId))
                .whereIn("receiverId", Arrays.asList(currentUserId, userId))
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshots != null) {
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
                    }
                });
    }

    private void saveMessagesToTextFile() {
        messageRef
                .whereIn("senderId", Arrays.asList(currentUserId, userId))
                .whereIn("receiverId", Arrays.asList(currentUserId, userId))
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        writeFileToStorage();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error fetching messages", e);
                        Toast.makeText(Chat.this, "Failed to fetch messages", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private File createTextFile() throws IOException {
        // Create a text file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String textFileName = "Chat_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File textFile = File.createTempFile(
                textFileName,  /* prefix */
                ".txt",        /* suffix */
                storageDir     /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        String currentTextFilePath = textFile.getAbsolutePath();
        return textFile;
    }

    private void writeFileToStorage() {
        messageRef
                .whereIn("senderId", Arrays.asList(currentUserId, userId))
                .whereIn("receiverId", Arrays.asList(currentUserId, userId))
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        StringBuilder data = new StringBuilder();
                        for (Message message : queryDocumentSnapshots.toObjects(Message.class)) {
                            data.append("Timestamp: ").append(message.getTimestamp()).append("\n");
                            data.append("Sender: ").append(message.getSenderId()).append("\n");
                            data.append("Receiver: ").append(message.getReceiverId()).append("\n");
                            data.append("Message: ").append(message.getContent()).append("\n\n");
                        }
                        try {
                            File textFile = createTextFile();
                            try (FileOutputStream fos = new FileOutputStream(textFile)) {
                                fos.write(data.toString().getBytes());
                                String title = "Messages Saved";
                                String message = "Messages saved to " + textFile.getAbsolutePath();
                                showNotification(title,message);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(Chat.this, "Failed to save messages", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error fetching messages", e);
                        Toast.makeText(Chat.this, "Failed to fetch messages", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.e(TAG, "Error creating image file", ex);
                }
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.GameBuddy.gb.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Load the image from the file
            File imgFile = new File(currentPhotoPath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                saveImageToStorage(bitmap);
            }
        }
    }

    private void saveImageToStorage(Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            // Rotate the bitmap to correct orientation
            Bitmap rotatedBitmap = rotateBitmap(bitmap, currentPhotoPath);

            // Save the rotated bitmap to storage
            out = new FileOutputStream(currentPhotoPath);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            String title = "Image Saved";
            String message = "Image saved to " + currentPhotoPath;
            showNotification(title,message);
        } catch (IOException e) {
            Log.e(TAG, "Error saving image", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing output stream", e);
            }
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, String photoPath) {
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(photoPath);
        } catch (IOException e) {
            Log.e(TAG, "Error reading exif data", e);
            return bitmap;
        }

        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            default:
                return bitmap;
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Message Channel";
            String description = "Channel for saving messages";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title ,String message) {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }
}
