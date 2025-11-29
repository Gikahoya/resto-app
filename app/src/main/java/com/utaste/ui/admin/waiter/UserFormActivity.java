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
import com.utaste.domain.user.Role; // ✅ AJOUT
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
        formTitle = findViewById(R.id.formTitle); // ✅ AJOUT : liaison du titre

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
            // ✅ MODIFICATION : Mise à jour du titre
            String userType = capitalize(currentUser.role.name());
            formTitle.setText(userType + " Details");

            edtFirst.setText(currentUser.firstName);
            edtLast.setText(currentUser.lastName);
            edtEmail.setText(currentUser.email);
            edtPwd.setHint("New password (optional)");

            // Gérer les permissions
            boolean isWaiter = currentUser.role == Role.WAITER;
            edtFirst.setEnabled(isWaiter);
            edtLast.setEnabled(isWaiter);
            edtEmail.setEnabled(isWaiter);
            btnDelete.setVisibility(isWaiter ? View.VISIBLE : View.GONE);
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupCreateMode() {
        formTitle.setText("New Waiter"); // En mode création, on ne peut créer qu'un serveur
        btnDelete.setVisibility(View.GONE);
    }

    private void save() {
        txtError.setText("");
        try {
            if (currentUser == null) { // Mode création
                ServiceLocator.waiters().create(
                        edtFirst.getText().toString(),
                        edtLast.getText().toString(),
                        edtEmail.getText().toString(),
                        edtPwd.getText().toString()
                );
                Toast.makeText(this, "Waiter created", Toast.LENGTH_SHORT).show();
            } else { // Mode mise à jour
                String newPwd = edtPwd.getText().toString();
                // Mise à jour mot de passe pour tous
                if (newPwd != null && !newPwd.isBlank()) {
                    currentUser.password = newPwd;
                }
                // Mise à jour des autres champs uniquement pour les serveurs
                if(currentUser.role == Role.WAITER) {
                    ServiceLocator.waiters().update(
                            oldEmail,
                            edtFirst.getText().toString(),
                            edtLast.getText().toString(),
                            edtEmail.getText().toString(),
                            edtPwd.getText().toString()
                    );
                } else {
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

    /**
     * Helper pour mettre la première lettre en majuscule.
     * "WAITER" -> "Waiter"
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
