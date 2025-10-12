package com.utaste;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.data.memory.InMemoryUserRepository;
import com.utaste.domain.user.Admin;
import com.utaste.domain.user.Chef;
import com.utaste.domain.user.Waiter;
import com.utaste.domain.user.Credentials;
import com.utaste.domain.user.User;
import com.utaste.ui.AdminMenuActivity;
import com.utaste.ui.ChefMenuActivity;
import com.utaste.ui.WaiterMenuActivity;

public class WelcomeActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        username = findViewById(R.id.username_id);
        password = findViewById(R.id.password_id);
        loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = username.getText().toString().trim();
                String passInput = password.getText().toString().trim();

                if (userInput.isEmpty() || passInput.isEmpty()) {
                    Toast.makeText(WelcomeActivity.this, "Please enter both username and password",
                            Toast.LENGTH_SHORT).show();
                    username.setText("");
                    password.setText("");
                    username.requestFocus();
                    return;
                }

                InMemoryUserRepository repo = InMemoryUserRepository.getInstance();
                Credentials creds = new Credentials(userInput, passInput);
                User user = repo.findByCredentials(creds);

                if (user == null) {
                    Toast.makeText(WelcomeActivity.this, "Invalid credentials",
                            Toast.LENGTH_SHORT).show();
                    username.setText("");
                    password.setText("");
                    username.requestFocus();
                } else {
                    // Redirection selon le rôle
                    Intent intent;
                    if (user instanceof Admin) {
                        intent = new Intent(WelcomeActivity.this, AdminMenuActivity.class);
                    } else if (user instanceof Chef) {
                        intent = new Intent(WelcomeActivity.this, ChefMenuActivity.class);
                    } else if (user instanceof Waiter) {
                        intent = new Intent(WelcomeActivity.this, WaiterMenuActivity.class);
                    } else {
                        Toast.makeText(WelcomeActivity.this, "Unknown role",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
