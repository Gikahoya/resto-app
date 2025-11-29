package com.utaste.ui.admin.waiter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.ServiceLocator;
import com.utaste.domain.user.Role;
import com.utaste.domain.user.User;

public class UserFormActivity extends AppCompatActivity {

    private EditText edtFirst, edtLast, edtEmail, edtPwd;
    private TextView txtError;
    private TextView formTitle;
    private Button btnSave, btnDelete;
    private String oldEmail;
    private User currentUser;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_user_form);

        // --- Liaison des vues ---
        edtFirst = findViewById(R.id.edtFirst);
        edtLast  = findViewById(R.id.edtLast);
        edtEmail = findViewById(R.id.edtEmail);
        edtPwd   = findViewById(R.id.edtPwd);
        txtError = findViewById(R.id.txtError);
        btnSave  = findViewById(R.id.btnSave);
        btnDelete= findViewById(R.id.btnDelete);
        formTitle = findViewById(R.id.formTitle);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        oldEmail = getIntent().getStringExtra("email");

        if (oldEmail != null) {
            // --- Mode Édition ---
            loadUserForEdit(oldEmail);
        } else {
            // --- Mode Création ---
            setupCreateMode();
        }

        btnSave.setOnClickListener(v -> save());
        btnDelete.setOnClickListener(v -> doDelete());
    }

    private void loadUserForEdit(String identifier) {
        // On cherche par email ou par ID
        currentUser = ServiceLocator.getUserRepository().findByEmail(identifier);
        if (currentUser == null) {
            currentUser = ServiceLocator.getUserRepository().findById(identifier);
        }

        if (currentUser != null) {
            // Titre dynamique en fonction du rôle
            String userType = capitalize(currentUser.role.name());
            formTitle.setText(userType + " Details");

            edtFirst.setText(currentUser.firstName);
            edtLast.setText(currentUser.lastName);
            edtEmail.setText(currentUser.email);
            edtPwd.setHint("New password (optional)");

            // 👉 On laisse TOUT éditable pour tous les rôles
            edtFirst.setEnabled(true);
            edtLast.setEnabled(true);
            edtEmail.setEnabled(true);

            // On garde le delete seulement pour les waiters
            boolean isWaiter = currentUser.role == Role.WAITER;
            btnDelete.setVisibility(isWaiter ? View.VISIBLE : View.GONE);
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupCreateMode() {
        // En mode création, on crée un serveur
        formTitle.setText("New Waiter");
        btnDelete.setVisibility(View.GONE);
    }

    private void save() {
        txtError.setText("");

        String first = edtFirst.getText().toString().trim();
        String last  = edtLast.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String newPwd = edtPwd.getText().toString();

        try {
            if (currentUser == null) { // --- Mode création (waiter) ---
                ServiceLocator.waiters().create(
                        first,
                        last,
                        email,
                        newPwd
                );
                Toast.makeText(this, "Waiter created", Toast.LENGTH_SHORT).show();
            } else { // --- Mode mise à jour ---
                // On met à jour les champs pour TOUS les rôles
                currentUser.firstName = first;
                currentUser.lastName  = last;
                currentUser.email     = email;

                if (newPwd != null && !newPwd.isBlank()) {
                    currentUser.password = newPwd;
                }

                if (currentUser.role == Role.WAITER) {
                    // Utilise le service spécial pour les waiters
                    ServiceLocator.waiters().update(
                            oldEmail,    // ancien email pour le retrouver
                            first,
                            last,
                            email,
                            newPwd
                    );
                } else {
                    // Admin / Chef : update via le repository générique
                    ServiceLocator.getUserRepository().updateUser(currentUser);
                }

                Toast.makeText(this, "Modifications saved", Toast.LENGTH_SHORT).show();
            }
            finish();
        } catch (IllegalArgumentException ex) {
            txtError.setText(ex.getMessage());
        }
    }

    private void doDelete() {
        if (currentUser == null || currentUser.role != Role.WAITER) return;

        // Règle: ne pas supprimer le dernier serveur
        if (ServiceLocator.waiters().list().size() <= 1) {
            Toast.makeText(this, "Cannot delete the last waiter.", Toast.LENGTH_LONG).show();
            return;
        }

        txtError.setText("");
        try {
            ServiceLocator.waiters().delete(currentUser.email);
            Toast.makeText(this, "Waiter has been deleted", Toast.LENGTH_SHORT).show();
            finish();
        } catch (IllegalArgumentException ex) {
            txtError.setText(ex.getMessage());
        }
    }

    /** "WAITER" -> "Waiter" */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
