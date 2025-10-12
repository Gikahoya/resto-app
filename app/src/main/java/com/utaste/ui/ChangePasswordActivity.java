package com.utaste.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentPwd = findViewById(R.id.current_pwd_id);
        newPwd = findViewById(R.id.new_pwd_id);
        confirmPwd = findViewById(R.id.confirm_pwd_id);
        saveButton = findViewById(R.id.save_button);

        // Récupère le username envoyé depuis le menu précédent
        String username = getIntent().getStringExtra("username");

        // Initialise le repository (base en mémoire)
        InMemoryUserRepository repo = InMemoryUserRepository.getInstance();

        // Récupère l'utilisateur par ID (username)
        User currentUser = repo.findById(username);

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

                if (currentUser == null) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "User not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Vérifie que l'ancien mot de passe est correct
                if (!currentUser.password.equals(oldPass)) {
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
                currentUser.password = newPass;
                repo.updateUser(currentUser);

                Toast.makeText(ChangePasswordActivity.this,
                        "Password successfully changed!", Toast.LENGTH_SHORT).show();

                // Retourne au bon menu selon le rôle
                Intent intent;
                if (currentUser instanceof Admin) {
                    intent = new Intent(ChangePasswordActivity.this, AdminMenuActivity.class);
                } else if (currentUser instanceof Chef) {
                    intent = new Intent(ChangePasswordActivity.this, ChefMenuActivity.class);
                } else if (currentUser instanceof Waiter) {
                    intent = new Intent(ChangePasswordActivity.this, WaiterMenuActivity.class);
                } else {
                    intent = new Intent(ChangePasswordActivity.this, WelcomeActivity.class);
                }

                intent.putExtra("username", username);
                startActivity(intent);
                finish();
            }
        });
    }
}
