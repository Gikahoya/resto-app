package com.utaste;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

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
                String user = username.getText().toString();
                String pass = password.getText().toString();

                if (user.trim().equals("admin") && pass.trim().equals("admin-pwd")) {
                    Intent intent = new Intent(WelcomeActivity.this, AdminMenuActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if (user.trim().equals("chef") && pass.trim().equals("chef-pwd")) {
                    Intent intent = new Intent(WelcomeActivity.this, ChefMenuActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if ((user.trim().equals("waiter1") || user.trim().equals("waiter2")) && pass.trim().equals("waiter-pwd")) {
                    Intent intent = new Intent(WelcomeActivity.this, WaiterMenuActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if (user.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(WelcomeActivity.this, "Please enter both username and password",
                            Toast.LENGTH_SHORT).show();
                    username.setText("");
                    password.setText("");
                    username.requestFocus();
                }
                else {
                    Toast.makeText(WelcomeActivity.this, "Invalid credentials",
                            Toast.LENGTH_SHORT).show();
                    username.setText("");
                    password.setText("");
                    username.requestFocus();
                }
            }
        });
    }
}