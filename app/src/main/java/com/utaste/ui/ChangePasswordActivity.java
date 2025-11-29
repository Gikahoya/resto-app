package com.utaste.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.utaste.R;
import com.utaste.domain.user.UserRepository;
import com.utaste.data.memory.InMemoryUserRepository; // MODIFIÉ : Importe la bonne classe

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText userIdEditText;
    private EditText newPasswordEditText;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // MODIFIÉ : Utilise la bonne classe pour obtenir l'instance
        userRepository = InMemoryUserRepository.getInstance();

        userIdEditText = findViewById(R.id.user_id_edit_text);
        newPasswordEditText = findViewById(R.id.new_password_edit_text);
        Button saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(v -> {
            String userIdOrEmail = userIdEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString().trim();

            if (userIdOrEmail.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            // MODIFIÉ : Assure que la méthode changePassword est appelée correctement
            // Note : La méthode `changePassword` est dans `InMemoryUserRepository`
            if (userRepository instanceof InMemoryUserRepository) {
                boolean success = ((InMemoryUserRepository) userRepository).changePassword(userIdOrEmail, newPassword);

                if (success) {
                    Toast.makeText(this, "Mot de passe changé avec succès", Toast.LENGTH_SHORT).show();
                    finish(); // Ferme l'activité après le succès
                } else {
                    Toast.makeText(this, "Utilisateur non trouvé", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Erreur: Le repository ne supporte pas cette opération", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
