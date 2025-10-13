package com.utaste.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.WelcomeActivity;
import com.utaste.data.memory.InMemoryUserRepository;
import com.utaste.domain.user.User;
import com.utaste.domain.user.Admin;
import com.utaste.domain.user.Chef;
import com.utaste.domain.user.Waiter;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText currentPwd;
    private EditText newPwd;
    private EditText confirmPwd;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        currentPwd = findViewById(R.id.current_pwd_id);
        newPwd = findViewById(R.id.new_pwd_id);
        confirmPwd = findViewById(R.id.confirm_pwd_id);
        saveButton = findViewById(R.id.save_button);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Récupère le username/email envoyé depuis le menu précédent
        String username = getIntent().getStringExtra("username");

        // Initialise le repository
        InMemoryUserRepository repo = InMemoryUserRepository.getInstance();

        // On cherche d'abord par ID (pour admin/chef)
        User currentUser = repo.findById(username);
        // Si non trouvé, on cherche par email (pour les waiters)
        if (currentUser == null) {
            currentUser = repo.findByEmail(username);
        }

        // On doit déclarer la variable finale pour l'utiliser dans le listener
        final User finalCurrentUser = currentUser;

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String oldPass = currentPwd.getText().toString().trim();
                String newPass = newPwd.getText().toString().trim();
                String confirmPass = confirmPwd.getText().toString().trim();

                if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (finalCurrentUser == null) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "User not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Vérifie que l'ancien mot de passe est correct
                if (!finalCurrentUser.password.equals(oldPass)) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    currentPwd.setText("");
                    currentPwd.requestFocus();
                    return;
                }

                // Vérifie que le nouveau mot de passe est confirmé
                if (!newPass.equals(confirmPass)) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Passwords do not match", Toast.LENGTH_SHORT).show();
                    newPwd.setText("");
                    confirmPwd.setText("");
                    newPwd.requestFocus();
                    return;
                }

                // Met à jour le mot de passe
                finalCurrentUser.password = newPass;
                repo.updateUser(finalCurrentUser);

                Toast.makeText(ChangePasswordActivity.this,
                        "Password successfully changed!", Toast.LENGTH_SHORT).show();

                // Retourne au bon menu selon le rôle
                Intent intent;
                if (finalCurrentUser instanceof Admin) {
                    intent = new Intent(ChangePasswordActivity.this, AdminMenuActivity.class);
                } else if (finalCurrentUser instanceof Chef) {
                    intent = new Intent(ChangePasswordActivity.this, ChefMenuActivity.class);
                } else if (finalCurrentUser instanceof Waiter) {
                    intent = new Intent(ChangePasswordActivity.this, WaiterMenuActivity.class);
                } else {
                    intent = new Intent(ChangePasswordActivity.this, WelcomeActivity.class);
                }

                // On repasse le même identifiant (username ou email) au menu suivant
                intent.putExtra("username", username);
                startActivity(intent);
                finishAffinity(); // Ferme cette activité et toutes les précédentes dans la pile
            }
        });
    }
}
