package com.utaste.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.utaste.R;
import com.utaste.WelcomeActivity;

public class WaiterMenuActivity extends AppCompatActivity {

    private Button logoutButton;
    private Button changePwdButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_waiter_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logoutButton = findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WaiterMenuActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        changePwdButton = findViewById(R.id.change_pwd);
        changePwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WaiterMenuActivity.this, ChangePasswordActivity.class);

                // On récupère l'identifiant passé par WelcomeActivity et on le transmet
                String username = getIntent().getStringExtra("username");
                intent.putExtra("username", username);

                startActivity(intent);
            }
        });
    }
}
