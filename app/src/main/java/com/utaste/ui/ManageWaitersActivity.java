package com.utaste.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.utaste.R;
import com.utaste.data.memory.InMemoryUserRepository;
import com.utaste.domain.user.User;

public class ManageWaitersActivity extends AppCompatActivity {

    private RecyclerView rvWaiters;
    private WaiterAdapter adapter;
    private InMemoryUserRepository repo;
    private TextView txtEmpty;

    private final ActivityResultLauncher<Intent> addWaiterLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                updateWaiterList();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiter_list);

        rvWaiters = findViewById(R.id.rvWaiters);
        txtEmpty = findViewById(R.id.txtEmpty);
        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        repo = InMemoryUserRepository.getInstance();

        setupRecyclerView();
        updateWaiterList();

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(ManageWaitersActivity.this, AddWaiterActivity.class);
            addWaiterLauncher.launch(intent);
        });
    }

    private void setupRecyclerView() {
        adapter = new WaiterAdapter(repo.getAllUsers(), waiter -> {
            repo.deleteUser(waiter.id);
            Toast.makeText(this, "Waiter " + waiter.firstName + " deleted", Toast.LENGTH_SHORT).show();
            updateWaiterList();
        });
        rvWaiters.setLayoutManager(new LinearLayoutManager(this));
        rvWaiters.setAdapter(adapter);
    }

    private void updateWaiterList() {
        adapter.updateWaiters(repo.getAllUsers());
        if (adapter.getItemCount() == 0) {
            txtEmpty.setVisibility(View.VISIBLE);
            rvWaiters.setVisibility(View.GONE);
        } else {
            txtEmpty.setVisibility(View.GONE);
            rvWaiters.setVisibility(View.VISIBLE);
        }
    }
}
