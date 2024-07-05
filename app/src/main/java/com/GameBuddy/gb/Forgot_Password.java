package com.GameBuddy.gb;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class Forgot_Password extends AppCompatActivity {

    AppCompatButton reset;
    TextInputEditText email;
    FirebaseAuth auth;
    TextView login;

    private static final String CHANNEL_ID = "message_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();

        reset = findViewById(R.id.btn_reset);
        email = findViewById(R.id.email);
        auth = FirebaseAuth.getInstance();
        login = findViewById(R.id.Login);

        reset.setOnClickListener(v -> {
            reset.setEnabled(false);
            String emailTxt = Objects.requireNonNull(email.getText()).toString().trim();
            if (emailTxt.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter Email Address", Toast.LENGTH_SHORT).show();
            }else{
                auth.sendPasswordResetEmail(emailTxt).addOnCompleteListener(n -> {
                    if (n.isSuccessful()) {
                        String title = "Password Reset";
                        String message = "Password Reset Email Sent";
                        showNotification(title,message);
                        Toast.makeText(Forgot_Password.this, "Password Reset Email Sent", Toast.LENGTH_SHORT).show();
                        reset.setEnabled(true);
                        Intent intent = new Intent(Forgot_Password.this, Login.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    }else{
                        Toast.makeText(Forgot_Password.this, Objects.requireNonNull(n.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        reset.setEnabled(true);
                    }
                });
            }
        });

        login.setOnClickListener(v -> {
            Intent intent = new Intent(Forgot_Password.this, Login.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

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