package com.utaste;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.utaste.data.sqlite.DataBaseHelper;
import com.utaste.data.sqlite.UserDao;
import com.utaste.ui.AdminMenuActivity;
import com.utaste.ui.ChefMenuActivity;
import com.utaste.ui.WaiterMenuActivity;

public class WelcomeActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        usernameEditText = findViewById(R.id.username_id);
        passwordEditText = findViewById(R.id.password_id);
        loginButton = findViewById(R.id.login_button);

        // Initialiser le UserDao qui utilise la base de données SQLite
        userDao = new UserDao(this);

        // Méthode pour peupler la base de données avec les utilisateurs par défaut si nécessaire
        createDefaultUsersIfNeeded();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });
    }

    /**
     * Crée les utilisateurs par défaut (admin, chef) UNIQUEMENT si la base de données est vide.
     */
    private void createDefaultUsersIfNeeded() {
        // On vérifie si l'admin existe déjà. Si non, on crée les utilisateurs par défaut.
        if (!userDao.exists("admin")) {
            // Ces informations ne sont utilisées qu'UNE SEULE FOIS pour initialiser la DB.
            userDao.insertIfAbsent("admin", "admin", "admin", "admin-pwd", "Admin");
            Toast.makeText(this, "Default user 'admin' created.", Toast.LENGTH_SHORT).show();
        }
        if (!userDao.exists("chef")) {
            // Ces informations ne sont utilisées qu'UNE SEULE FOIS pour initialiser la DB.
            userDao.insertIfAbsent("chef", "chef", "chef", "chef-pwd", "Chef");
            Toast.makeText(this, "Default user 'chef' created.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(WelcomeActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Utiliser le UserDao pour trouver l'utilisateur par son 'username' qui sert d'email/id ici
        Cursor cursor = userDao.getByEmail(username);

        if (cursor != null && cursor.moveToFirst()) {
            String dbPassword = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_USER_PWD));
            String dbRole = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHelper.COL_USER_ROLE));
            cursor.close();

            if (password.equals(dbPassword)) {
                // Le mot de passe est correct, on redirige
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                redirectToMenu(username, dbRole);
            } else {
                // Mauvais mot de passe
                showLoginError();
            }
        } else {
            // Utilisateur non trouvé
            if (cursor != null) {
                cursor.close();
            }
            showLoginError();
        }
    }

    private void redirectToMenu(String username, String role) {
        Intent intent;
        if ("Admin".equalsIgnoreCase(role)) {
            intent = new Intent(WelcomeActivity.this, AdminMenuActivity.class);
        } else if ("Chef".equalsIgnoreCase(role)) {
            intent = new Intent(WelcomeActivity.this, ChefMenuActivity.class);
        } else if ("Waiter".equalsIgnoreCase(role)) {
            intent = new Intent(WelcomeActivity.this, WaiterMenuActivity.class);
        } else {
            Toast.makeText(WelcomeActivity.this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
            return;
        }
        intent.putExtra("username", username);
        startActivity(intent);
        finish(); // Empêche l'utilisateur de revenir à l'écran de connexion avec le bouton "retour"
    }

    private void showLoginError() {
        Toast.makeText(WelcomeActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        usernameEditText.setText("");
        passwordEditText.setText("");
        usernameEditText.requestFocus();
    }
}