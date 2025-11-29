package com.utaste.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.utaste.R;
import com.utaste.WelcomeActivity;
import com.utaste.data.sqlite.DataBaseHelper;
import com.utaste.ui.admin.waiter.WaiterListActivity;

public class AdminMenuActivity extends AppCompatActivity {

    private Button logoutButton;
    private Button changePwdButton;
    private Button manageWaitersButton;
    private Button resetDbButton;
    private Button changeProfileButton;
    private Button resetOtherPwdButton;

    private DataBaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // DB helper (pour le reset)
        dbHelper = new DataBaseHelper(this);

        // --- Boutons ---
        logoutButton        = findViewById(R.id.logout);
        changePwdButton     = findViewById(R.id.change_pwd);
        manageWaitersButton = findViewById(R.id.create_waiter);
        resetDbButton       = findViewById(R.id.reset_db);
        changeProfileButton = findViewById(R.id.change_profile);
        resetOtherPwdButton = findViewById(R.id.reset_pwd);

        // Logout
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMenuActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Change own password
        changePwdButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMenuActivity.this, ChangePasswordActivity.class);
            String username = getIntent().getStringExtra("username");
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Manage waiters
        manageWaitersButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMenuActivity.this, WaiterListActivity.class);
            startActivity(intent);
        });

        //  message d’avertissement
        resetDbButton.setOnClickListener(v -> showResetDatabaseDialog());
    }

    private void showResetDatabaseDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset database")
                .setMessage(
                        "This will DELETE ALL recipes, ingredients and sales.\n\n" +
                                "This action cannot be undone.\n\n" +
                                "Are you sure you want to continue?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reset", (dialog, which) -> {
                    dbHelper.resetDatabase();
                    Toast.makeText(
                            this,
                            "Database has been reset successfully.",
                            Toast.LENGTH_LONG
                    ).show();
                })
                .show();
    }
}
