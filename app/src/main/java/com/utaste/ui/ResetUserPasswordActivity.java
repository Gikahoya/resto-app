package com.utaste.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.data.memory.InMemoryUserRepository;
import com.utaste.domain.user.Admin;
import com.utaste.domain.user.Chef;
import com.utaste.domain.user.Waiter;
import com.utaste.domain.user.User;

public class ResetUserPasswordActivity extends AppCompatActivity {

    private EditText userKeyEdit;
    private InMemoryUserRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_user_password);

        userKeyEdit = findViewById(R.id.user_key_id);
        Button resetButton = findViewById(R.id.reset_button);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        repo = InMemoryUserRepository.getInstance();

        resetButton.setOnClickListener(v -> {
            String key = userKeyEdit.getText().toString().trim();
            if (key.isEmpty()) {
                Toast.makeText(this, "Enter user id or email", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = repo.findById(key);
            if (user == null) {
                user = repo.findByEmail(key);
            }

            if (user == null) {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                return;
            }

            String defaultPwd;
            if (user instanceof Admin) {
                defaultPwd = "admin-pwd";
            } else if (user instanceof Chef) {
                defaultPwd = "chef-pwd";
            } else if (user instanceof Waiter) {
                defaultPwd = "waiter-pwd";
            } else {
                Toast.makeText(this, "Unknown role", Toast.LENGTH_SHORT).show();
                return;
            }

            user.password = defaultPwd;
            repo.updateUser(user);

            Toast.makeText(this, "Password reset to default", Toast.LENGTH_SHORT).show();
        });
    }
}
