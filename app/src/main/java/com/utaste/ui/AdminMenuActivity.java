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
// ✅ AJOUT : Import manquant pour ManageUsersActivity
import com.utaste.ui.admin.ManageUsersActivity;
import com.utaste.ui.admin.waiter.WaiterListActivity;

public class AdminMenuActivity extends AppCompatActivity {

    // Déclaration propre des variables pour les boutons
    private Button logoutButton;
    private Button changePwdButton;
    private Button manageUsersButton;
    private Button resetDbButton;
    private Button resetOtherPwdButton; // Bouton qui était dans la branche de "resetDatabase"

    // Helper pour la base de données
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

        // Initialisation du DB helper
        dbHelper = new DataBaseHelper(this);

        // --- Liaison unique et propre de tous les boutons ---
        logoutButton = findViewById(R.id.logout);
        changePwdButton = findViewById(R.id.change_pwd);
        resetDbButton = findViewById(R.id.reset_db);
        manageUsersButton = findViewById(R.id.change_profile); // Votre bouton "Manage Users"

        // ❌ NOTE : Les deux boutons ci-dessous n'existent pas dans le dernier layout XML fourni (activity_admin_menu.xml).
        // Je les commente pour éviter un crash. Si vous les ajoutez au XML, décommentez les lignes.
        // manageWaitersButton = findViewById(R.id.create_waiter);
        // resetOtherPwdButton = findViewById(R.id.reset_pwd);


        // --- Configuration des Listeners (un par bouton) ---

        // Listener pour le bouton Logout
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMenuActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Listener pour "Change own password"
        changePwdButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMenuActivity.this, ChangePasswordActivity.class);
            String username = getIntent().getStringExtra("username");
            intent.putExtra("username", username);
            startActivity(intent);
        });

        manageUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMenuActivity.this, ManageUsersActivity.class);
            startActivity(intent);
        });

        // Il n'y a plus qu'un seul listener qui affiche la boîte de dialogue de confirmation.
        resetDbButton.setOnClickListener(v -> showResetDatabaseDialog());

        /*
        // Listener pour le bouton "Manage Waiters" (si vous le remettez dans le XML)
        if (manageWaitersButton != null) {
            manageWaitersButton.setOnClickListener(v -> {
                Intent intent = new Intent(AdminMenuActivity.this, WaiterListActivity.class);
                startActivity(intent);
            });
        }
        */
    }

    /**
     * Affiche la boîte de dialogue de confirmation pour réinitialiser la base de données.
     * Cette méthode vient de la branche "resetDatabase".
     */
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
