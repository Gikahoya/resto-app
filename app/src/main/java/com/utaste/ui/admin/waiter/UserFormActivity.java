package com.utaste.ui.admin.waiter;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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

        // --- Bind views ---
        edtFirst  = findViewById(R.id.edtFirst);
        edtLast   = findViewById(R.id.edtLast);
        edtEmail  = findViewById(R.id.edtEmail);
        edtPwd    = findViewById(R.id.edtPwd);
        txtError  = findViewById(R.id.txtError);
        btnSave   = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        formTitle = findViewById(R.id.formTitle);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        oldEmail = getIntent().getStringExtra("email");

        if (oldEmail != null) {
            // --- Mode édition ---
            loadUserForEdit(oldEmail);
        } else {
            // --- Mode création ---
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
            // Titre dynamique selon le rôle
            String userType = capitalize(currentUser.role.name());
            formTitle.setText(userType + " Details");

            edtFirst.setText(currentUser.firstName);
            edtLast.setText(currentUser.lastName);
            edtEmail.setText(currentUser.email);
            edtPwd.setHint("New password (optional)");

            // Tous les rôles peuvent modifier leurs infos
            edtFirst.setEnabled(true);
            edtLast.setEnabled(true);
            edtEmail.setEnabled(true);

            // Bouton Delete seulement pour les serveurs
            boolean isWaiter = currentUser.role == Role.WAITER;
            btnDelete.setVisibility(isWaiter ? View.VISIBLE : View.GONE);
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupCreateMode() {
        // Création = nouveau serveur (waiter)
        formTitle.setText("New Waiter");
        btnDelete.setVisibility(View.GONE);
        currentUser = null;
    }

    private void save() {
        txtError.setText("");

        String first  = edtFirst.getText().toString().trim();
        String last   = edtLast.getText().toString().trim();
        String email  = edtEmail.getText().toString().trim();
        String newPwd = edtPwd.getText().toString();

        // ===== VALIDATION GÉNÉRALE =====
        if (TextUtils.isEmpty(first)) {
            edtFirst.setError("First name is required");
            edtFirst.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(last)) {
            edtLast.setError("Last name is required");
            edtLast.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Email is required");
            edtEmail.requestFocus();
            return;
        }

        // ✅ vraie validation d'email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Please enter a valid email address");
            edtEmail.requestFocus();
            return;
        }

        try {
            if (currentUser == null) {
                // ===== MODE CRÉATION : password OBLIGATOIRE =====
                if (TextUtils.isEmpty(newPwd)) {
                    edtPwd.setError("Password is required");
                    edtPwd.requestFocus();
                    return;
                }

                // Création = nouveau waiter
                ServiceLocator.waiters().create(
                        first,
                        last,
                        email,
                        newPwd
                );
                Toast.makeText(this, "Waiter created", Toast.LENGTH_SHORT).show();

            } else {
                // ===== MODE MISE À JOUR =====
                // Mettre à jour les champs pour TOUS les rôles
                currentUser.firstName = first;
                currentUser.lastName  = last;
                currentUser.email     = email;

                if (!TextUtils.isEmpty(newPwd)) {
                    currentUser.password = newPwd;
                }

                if (currentUser.role == Role.WAITER) {
                    // Waiter : passer aussi par le service dédié
                    ServiceLocator.waiters().update(
                            oldEmail,
                            first,
                            last,
                            email,
                            newPwd
                    );
                } else {
                    // Admin / Chef : update via le UserRepository
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

        // Ne pas supprimer le dernier serveur
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
