package com.utaste.app.chef;

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
import com.utaste.data.sqlite.IngredientDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.ui.recipe.IngredientSimpleAdapter;

import java.util.List;

public class IngredientListActivity extends AppCompatActivity {

    private RecyclerView rvIngredients;
    private TextView tvEmpty;
    private IngredientDao ingredientDao;
    private IngredientSimpleAdapter adapter;

    private final ActivityResultLauncher<Intent> createIngredientLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                loadIngredients();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_list);

        ingredientDao = new IngredientDao(this);
        initViews();
        setupRecyclerView();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.fabAddIngredient).setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateIngredientsActivity.class);
            createIngredientLauncher.launch(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadIngredients();
    }

    private void initViews() {
        rvIngredients = findViewById(R.id.rvIngredients);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void setupRecyclerView() {
        adapter = new IngredientSimpleAdapter(ingredient -> {
            Intent intent = new Intent(this, IngredientDetailActivity.class);
            intent.putExtra("INGREDIENT_ID", ingredient.getId());
            startActivity(intent);
        });
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvIngredients.setAdapter(adapter);
    }

    private void loadIngredients() {
        new Thread(() -> {
            List<Ingredient> ingredients = ingredientDao.getAll();
            runOnUiThread(() -> {
                adapter.submitList(ingredients);
                tvEmpty.setVisibility(ingredients.isEmpty() ? View.VISIBLE : View.GONE);
                rvIngredients.setVisibility(ingredients.isEmpty() ? View.GONE : View.VISIBLE);
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ingredientDao != null) {
            ingredientDao.close();
        }
    }
}