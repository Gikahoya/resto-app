package com.utaste.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.utaste.R;
import com.utaste.ServiceLocator;
import com.utaste.domain.user.User;
import com.utaste.ui.admin.waiter.UserFormActivity;

import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private UserAdapter adapter;
    private TextView txtEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        initViews();
        setupRecyclerView();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void initViews() {
        rvUsers = findViewById(R.id.rvUsers);
        txtEmpty = findViewById(R.id.txtEmpty);
    }

    private void setupRecyclerView() {
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(user -> {
            // Un clic sur un utilisateur ouvre le formulaire de détails
            // L'ID/email est passé pour charger les données
            Intent intent = new Intent(this, UserFormActivity.class);
            String identifier = user.email != null ? user.email : user.id;
            intent.putExtra("email", identifier);
            startActivity(intent);
        });
        rvUsers.setAdapter(adapter);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        FloatingActionButton fab = findViewById(R.id.fabAddWaiter);
        fab.setOnClickListener(v -> {
            // Le bouton + ouvre le formulaire en mode création (sans passer d'email)
            Intent intent = new Intent(this, UserFormActivity.class);
            startActivity(intent);
        });
    }

    private void loadUsers() {
        List<User> users = ServiceLocator.getUserRepository().getAllUsers();
        adapter.setData(users);
        txtEmpty.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
