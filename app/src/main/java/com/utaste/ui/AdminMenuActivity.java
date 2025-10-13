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
import com.utaste.ui.admin.waiter.WaiterListActivity;

public class AdminMenuActivity extends AppCompatActivity {

    private Button logoutButton;
    private Button changePwdButton;
    private Button manageWaitersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logoutButton = findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMenuActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        changePwdButton = findViewById(R.id.change_pwd);
        changePwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMenuActivity.this, ChangePasswordActivity.class);
                String username = getIntent().getStringExtra("username");
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        manageWaitersButton = findViewById(R.id.create_waiter);
        manageWaitersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminMenuActivity.this, WaiterListActivity.class);
                startActivity(intent);
            }
        });
    }
}