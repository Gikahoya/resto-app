package com.utaste.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.data.memory.InMemoryUserRepository;
import com.utaste.domain.user.User;

public class ChangeProfileActivity extends AppCompatActivity {

    private EditText firstNameEdit;
    private EditText lastNameEdit;
    private EditText emailEdit;

    private InMemoryUserRepository repo;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile);

        firstNameEdit = findViewById(R.id.firstname_id);
        lastNameEdit  = findViewById(R.id.lastname_id);
        emailEdit     = findViewById(R.id.email_id);
        Button saveButton = findViewById(R.id.save_button);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        repo = InMemoryUserRepository.getInstance();
        String key = getIntent().getStringExtra("username");

        // Retrouver l'utilisateur
        currentUser = repo.findById(key);
        if (currentUser == null) {
            currentUser = repo.findByEmail(key);
        }

        if (currentUser == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firstNameEdit.setText(currentUser.firstName);
        lastNameEdit.setText(currentUser.lastName);
        emailEdit.setText(currentUser.email);

        saveButton.setOnClickListener(v -> {
            String fn = firstNameEdit.getText().toString().trim();
            String ln = lastNameEdit.getText().toString().trim();
            String em = emailEdit.getText().toString().trim();

            if (em.isEmpty()) {
                Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
                return;
            }

            currentUser.firstName = fn;
            currentUser.lastName  = ln;
            currentUser.email     = em;

            repo.updateUser(currentUser);

            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
