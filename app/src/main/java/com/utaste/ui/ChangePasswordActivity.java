package com.utaste.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.data.sqlite.DataBaseHelper;
import com.utaste.data.sqlite.UserDao;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText currentPwd;
    private EditText newPwd;
    private EditText confirmPwd;
    private Button saveButton;
    private UserDao userDao;

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
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Error: User session not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialise le DAO pour interagir avec la base de données SQLite
        userDao = new UserDao(this);

        saveButton.setOnClickListener(v -> {
            String oldPass = currentPwd.getText().toString().trim();
            String newPass = newPwd.getText().toString().trim();
            String confirmPass = confirmPwd.getText().toString().trim();

            // 1. Valider que tous les champs sont remplis
            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(ChangePasswordActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Valider que le nouveau mot de passe est bien confirmé
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(ChangePasswordActivity.this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                newPwd.setText("");
                confirmPwd.setText("");
                newPwd.requestFocus();
                return;
            }

            // 3. Valider que l'ancien mot de passe est correct en le comparant à la base de données
            try (Cursor cursor = userDao.getByEmail(username)) {
                if (cursor == null || !cursor.moveToFirst()) {
                    Toast.makeText(ChangePasswordActivity.this, "Error: User not found in database.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String dbPassword = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_USER_PWD));
                if (!dbPassword.equals(oldPass)) {
                    Toast.makeText(ChangePasswordActivity.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    currentPwd.setText("");
                    currentPwd.requestFocus();
                    return; // On arrête ici si l'ancien mot de passe est faux
                }
            } // Le curseur est fermé automatiquement ici

            // 4. Si toutes les validations sont passées, on met à jour le mot de passe dans la base de données
            int rowsAffected = userDao.resetPassword(username, newPass);

            if (rowsAffected > 0) {
                Toast.makeText(ChangePasswordActivity.this, "Password successfully changed!", Toast.LENGTH_SHORT).show();
                finish(); // On ferme simplement l'activité, l'utilisateur retourne au menu précédent
            } else {
                Toast.makeText(ChangePasswordActivity.this, "Failed to change password. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}