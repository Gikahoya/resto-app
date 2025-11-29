package com.utaste.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.utaste.R;
import com.utaste.data.memory.InMemoryUserRepository;
import com.utaste.domain.user.User;
import com.utaste.domain.user.Waiter;

public class AddWaiterActivity extends AppCompatActivity {

    private EditText firstNameEdit, lastNameEdit, usernameEdit;
    private InMemoryUserRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_waiter);

        firstNameEdit = findViewById(R.id.firstname_edit);
        lastNameEdit = findViewById(R.id.lastname_edit);
        usernameEdit = findViewById(R.id.username_edit);
        Button addWaiterButton = findViewById(R.id.add_waiter_button);

        repo = InMemoryUserRepository.getInstance();

        addWaiterButton.setOnClickListener(v -> {
            String firstName = firstNameEdit.getText().toString().trim();
            String lastName = lastNameEdit.getText().toString().trim();
            String username = usernameEdit.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = username + "@utaste.com";

            if (repo.findById(username) != null || repo.findByEmail(email) != null) {
                Toast.makeText(this, "Username or email already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            User newWaiter = new Waiter(username, "waiter-pwd");
            newWaiter.firstName = firstName;
            newWaiter.lastName = lastName;
            newWaiter.email = email;

            try {
                repo.addUser(newWaiter);
                Toast.makeText(this, "Waiter added successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
