package com.utaste.ui.admin.waiter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.utaste.R;                               // ressources UI
import com.utaste.ServiceLocator;                  // service
import com.utaste.domain.user.User;                // Importez la classe User


public class WaiterFormActivity extends AppCompatActivity {
    private EditText edtFirst, edtLast, edtEmail, edtPwd; // champs du form
    private TextView txtError;                      // zone d’erreur
    private Button btnSave, btnDelete;              // actions
    private String oldEmail;                        // null = création ; non-null = édition

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);                            // cycle de vie
        setContentView(R.layout.activity_waiter_form);// layout du form

        edtFirst = findViewById(R.id.edtFirst);       // bind views
        edtLast  = findViewById(R.id.edtLast);
        edtEmail = findViewById(R.id.edtEmail);
        edtPwd   = findViewById(R.id.edtPwd);
        txtError = findViewById(R.id.txtError);
        btnSave  = findViewById(R.id.btnSave);
        btnDelete= findViewById(R.id.btnDelete);

        // ====================== ✨ ASSUREZ-VOUS QUE CETTE LIGNE EST PRÉSENTE ✨ ======================
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        // =======================================================================================

        oldEmail = getIntent().getStringExtra("email"); // lire param venant de la liste
        btnDelete.setVisibility(oldEmail == null ? View.GONE : View.VISIBLE); // Delete visible si édition

        if (oldEmail != null) {
            // --- MODE ÉDITION ---
            setTitle("Edit Waiter");

            User waiterToEdit = ServiceLocator.waiters().findByEmail(oldEmail);

            if (waiterToEdit != null) {
                edtFirst.setText(waiterToEdit.firstName);
                edtLast.setText(waiterToEdit.lastName);
                edtEmail.setText(waiterToEdit.email);
            }

            edtPwd.setHint("New password (optional)");
            edtEmail.setEnabled(true);
        } else {
            // --- MODE CRÉATION ---
            setTitle("Add Waiter");
        }


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { save(); }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { doDelete(); }
        });
    }

    private void save() {
        txtError.setText("");
        try {
            if (oldEmail == null) {
                // Création
                ServiceLocator.waiters().create(
                        edtFirst.getText().toString(),
                        edtLast.getText().toString(),
                        edtEmail.getText().toString(),
                        edtPwd.getText().toString()
                );
                Toast.makeText(this, "Waiter has been added", Toast.LENGTH_SHORT).show();
            } else {
                // Édition
                ServiceLocator.waiters().update(
                        oldEmail,                               // email d’origine (clé pour trouver l'user)
                        edtFirst.getText().toString(),
                        edtLast.getText().toString(),
                        edtEmail.getText().toString(),          // nouvel email (peut être le même)
                        edtPwd.getText().toString()
                );
                Toast.makeText(this, "Modifications saved", Toast.LENGTH_SHORT).show();
            }
            finish();
        } catch (IllegalArgumentException ex) {
            txtError.setText(ex.getMessage());
        }
    }

    private void doDelete() {
        txtError.setText("");
        try {
            ServiceLocator.waiters().delete(edtEmail.getText().toString());
            Toast.makeText(this, "Waiter has been deleted", Toast.LENGTH_SHORT).show();
            finish();
        } catch (IllegalArgumentException ex) {
            txtError.setText(ex.getMessage());
        }
    }
}
