package com.utaste.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.data.memory.InMemoryUserRepository;

public class AdminMenuActivity extends AppCompatActivity {

    private InMemoryUserRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);

        repo = InMemoryUserRepository.getInstance();

        Button changeProfileButton = findViewById(R.id.change_profile_button);
        Button manageWaitersButton = findViewById(R.id.manage_waiters_button);
        Button changePasswordButton = findViewById(R.id.change_password_button); // Assuming you have this button
        Button resetDataButton = findViewById(R.id.reset_data_button);

        changeProfileButton.setOnClickListener(v -> {
            Intent i = new Intent(this, ChangeProfileActivity.class);
            i.putExtra("username", "admin"); // L'admin modifie son propre profil
            startActivity(i);
        });

        manageWaitersButton.setOnClickListener(v -> {
            // Ligne corrigée
            Intent i = new Intent(this, com.utaste.ui.admin.waiter.WaiterListActivity.class);

        });

        changePasswordButton.setOnClickListener(v -> {
            // Ligne corrigée
            Intent i = new Intent(this, com.utaste.ui.admin.waiter.WaiterListActivity.class);

            i.putExtra("username", "admin");
            startActivity(i);
        });

        resetDataButton.setOnClickListener(v -> {
            repo.reset();
            Toast.makeText(this, "Data has been reset", Toast.LENGTH_SHORT).show();
        });
    }
}
