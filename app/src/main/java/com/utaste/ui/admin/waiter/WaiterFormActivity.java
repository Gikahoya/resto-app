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

        oldEmail = getIntent().getStringExtra("email"); // lire param venant de la liste
        btnDelete.setVisibility(oldEmail == null ? View.GONE : View.VISIBLE); // Delete visible si édition
        if (oldEmail != null) {                      // si édition
            edtEmail.setText(oldEmail);               // pré-remplir email
            edtEmail.setEnabled(false);               // choix humain: email non modifiable
        }

        btnSave.setOnClickListener(new View.OnClickListener() { // clic Enregistrer
            @Override public void onClick(View v) { save(); }     // appeler save()
        });
        btnDelete.setOnClickListener(new View.OnClickListener() { // clic Supprimer
            @Override public void onClick(View v) { doDelete(); }   // appeler delete
        });
    }

    private void save() {                           // créer ou modifier
        txtError.setText("");                         // effacer erreurs
        try {
            if (oldEmail == null) {                     // création
                ServiceLocator.waiters().create(
                        edtFirst.getText().toString(),          // prénom
                        edtLast.getText().toString(),           // nom
                        edtEmail.getText().toString(),          // email
                        edtPwd.getText().toString()             // mot de passe
                );
                Toast.makeText(this, "Waiter has been added", Toast.LENGTH_SHORT).show(); // feedback
            } else {                                    // édition
                ServiceLocator.waiters().update(
                        oldEmail,                               // email d’origine (clé)
                        edtFirst.getText().toString(),
                        edtLast.getText().toString(),
                        edtEmail.getText().toString(),          // restera le même (email verrouillé)
                        edtPwd.getText().toString()
                );
                Toast.makeText(this, "Modifications saved", Toast.LENGTH_SHORT).show(); // feedback
            }
            finish();                                   // fermer l’écran et revenir à la liste
        } catch (IllegalArgumentException ex) {       // erreurs métier (service)
            txtError.setText(ex.getMessage());          // afficher le message
        }
    }

    private void doDelete() {                       // suppression
        txtError.setText("");                         // effacer erreurs
        try {
            ServiceLocator.waiters().delete(            // supprimer par email
                    edtEmail.getText().toString()
            );
            Toast.makeText(this, "Waiter has been deleted", Toast.LENGTH_SHORT).show(); // feedback
            finish();                                   // retour à la liste
        } catch (IllegalArgumentException ex) {
            txtError.setText(ex.getMessage());          // afficher erreur
        }
    }
}
