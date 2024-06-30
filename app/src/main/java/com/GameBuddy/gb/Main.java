package com.GameBuddy.gb;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.GameBuddy.gb.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        com.GameBuddy.gb.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String fragmentToLoad = getIntent().getStringExtra("fragmentToLoad");
        if ("profileFragment".equals(fragmentToLoad)) {
            replaceFragment(new Profile_Fragment());
            binding.bottomNavigationView.setSelectedItemId(R.id.profile);
        } else {
            replaceFragment(new Chat_Fragment());
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.chat) {
                replaceFragment(new Chat_Fragment());
            } else if (item.getItemId() == R.id.search) {
                replaceFragment(new Search_Fragment());
            } else if (item.getItemId() == R.id.map) {
                replaceFragment(new Map_Fragment());
            } else if (item.getItemId() == R.id.profile) {
                replaceFragment(new Profile_Fragment());
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
}
