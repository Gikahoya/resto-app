package com.utaste.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;

public class WaiterMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiter_menu);

        Button changePasswordButton = findViewById(R.id.change_password_button);

        changePasswordButton.setOnClickListener(v -> {
            Intent i = new Intent(this, ChangePasswordActivity.class);
            // We need to get the current user's username to pass it to the activity
            String username = getIntent().getStringExtra("username");
            i.putExtra("username", username);
            startActivity(i);
        });
    }
}
