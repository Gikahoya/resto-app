package com.utaste.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.WelcomeActivity;
import com.utaste.data.memory.InMemoryUserRepository;
import com.utaste.domain.user.Admin;
import com.utaste.domain.user.Chef;
import com.utaste.domain.user.User;
import com.utaste.domain.user.Waiter;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText currentPwd, newPwd, confirmPwd;
    private Button saveButton;
    private String loggedInUsername; // L'ID de l'utilisateur ("admin", "chef", "john.doe", etc.)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // --- Liaison des vues ---
        currentPwd = findViewById(R.id.current_pwd_id);
        newPwd = findViewById(R.id.new_pwd_id);
        confirmPwd = findViewById(R.id.confirm_pwd_id);
        saveButton = findViewById(R.id.save_button);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Récupère l'ID de l'utilisateur passé depuis le menu précédent
        loggedInUsername = getIntent().getStringExtra("username");
        if (loggedInUsername == null) {
            Toast.makeText(this, "Error: User not identified.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // --- Logique du bouton "Save" ---
        saveButton.setOnClickListener(v -> attemptChangePassword());
    }

    private void attemptChangePassword() {
        String oldPass = currentPwd.getText().toString().trim();
        String newPass = newPwd.getText().toString().trim();
        String confirmPass = confirmPwd.getText().toString().trim();

        // 1. Validations de base
        if (TextUtils.isEmpty(oldPass) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Vérification et mise à jour
        InMemoryUserRepository repo = InMemoryUserRepository.getInstance();

        // On récupère l'utilisateur par son ID (qui est maintenant simple pour tout le monde)
        User currentUser = repo.findById(loggedInUsername);

        if (currentUser == null) {
            Toast.makeText(this, "Error: User not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Vérification du mot de passe actuel
        if (!currentUser.password.equals(oldPass)) {
            Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
            currentPwd.setText("");
            currentPwd.requestFocus();
            return;
        }

        // 4. Le mot de passe est correct, on le met à jour
        currentUser.password = newPass;
        repo.updateUser(currentUser);

        Toast.makeText(this, "Password successfully changed!", Toast.LENGTH_SHORT).show();

        // 5. Redirection vers le bon menu
        redirectToCorrectMenu(currentUser);
    }

    private void redirectToCorrectMenu(User user) {
        Intent intent;
        if (user instanceof Admin) {
            intent = new Intent(this, AdminMenuActivity.class);
        } else if (user instanceof Chef) {
            intent = new Intent(this, ChefMenuActivity.class);
        } else if (user instanceof Waiter) {
            intent = new Intent(this, WaiterMenuActivity.class);
        } else {
            // Sécurité : si le rôle est inconnu, on déconnecte
            intent = new Intent(this, WelcomeActivity.class);
        }

        intent.putExtra("username", user.id); // On passe toujours l'ID
        startActivity(intent);
        finishAffinity(); // Ferme tous les écrans précédents
    }
}
