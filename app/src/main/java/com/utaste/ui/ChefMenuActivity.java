package com.utaste.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;

public class ChefMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_menu);

        Button changePasswordButton = findViewById(R.id.change_password_button);

        changePasswordButton.setOnClickListener(v -> {
            Intent i = new Intent(this, ChangePasswordActivity.class);
            i.putExtra("username", "chef"); // Le chef modifie son propre mot de passe
            startActivity(i);
        });
    }
}
