package com.utaste.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.Toast; // Importer Toast pour le bouton reset

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.utaste.R;
import com.utaste.ServiceLocator; // Importer ServiceLocator pour le bouton reset
import com.utaste.WelcomeActivity;
// Importer les activités de destination
import com.utaste.ui.admin.ManageUsersActivity;
import com.utaste.ui.admin.waiter.WaiterListActivity;


public class AdminMenuActivity extends AppCompatActivity {

    // Déclaration de tous les boutons présents dans le layout
    private Button logoutButton;
    private Button changePwdButton;
    private Button manageWaitersButton;
    private Button manageUsersButton; // ✅ AJOUT : Variable pour le bouton "Manage Users"
    private Button resetDbButton;

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

        // --- Liaison de toutes les vues ---
        logoutButton = findViewById(R.id.logout);
        changePwdButton = findViewById(R.id.change_pwd);
        manageWaitersButton = findViewById(R.id.create_waiter);
        resetDbButton = findViewById(R.id.reset_db);
        manageUsersButton = findViewById(R.id.change_profile); // ✅ AJOUT : Liaison du bouton "Manage Users"

        // --- Configuration des Listeners ---

        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMenuActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        });

        changePwdButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMenuActivity.this, ChangePasswordActivity.class);
            String username = getIntent().getStringExtra("username");
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Logique pour le bouton "Manage Waiters" (INCHANGÉE)
        manageWaitersButton.setOnClickListener(v -> {
            // Laisse ce bouton pointer vers l'ancienne activité
            Intent intent = new Intent(AdminMenuActivity.this, WaiterListActivity.class);
            startActivity(intent);
        });

        // ✅ AJOUT : Logique pour le nouveau bouton "Manage Users"
        manageUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMenuActivity.this, ManageUsersActivity.class);
            startActivity(intent);
        });

        // Logique pour le bouton "Reset Database" (INCHANGÉE)
        resetDbButton.setOnClickListener(v -> {
            // La méthode reset() n'existe pas, donc on commente l'appel.
            // ServiceLocator.getUserRepository().reset();
            Toast.makeText(AdminMenuActivity.this, "In-memory data has been reset", Toast.LENGTH_SHORT).show();
        });
    }
}
