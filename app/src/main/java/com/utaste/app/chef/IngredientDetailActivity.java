package com.utaste.app.chef;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.data.sqlite.IngredientDao;
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.NutritionFact;

import java.util.List;
import java.util.Locale;

public class IngredientDetailActivity extends AppCompatActivity {

    private TextView tvIngredientName, tvRecipesUsed;
    private TextView tvEnergy, tvFat, tvSaturatedFat, tvCarbs, tvSugars, tvProtein, tvSalt;
    private Button btnAssign, btnModify, btnRemove, btnDelete;

    private IngredientDao ingredientDao;
    private RecipeDao recipeDao;
    private Ingredient currentIngredient;
    private int ingredientId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_detail);

        ingredientId = getIntent().getIntExtra("INGREDIENT_ID", -1);
        if (ingredientId == -1) {
            Toast.makeText(this, "Error: Ingredient not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ingredientDao = new IngredientDao(this);
        recipeDao = new RecipeDao(this);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadIngredientDetails();
    }

    private void initViews() {
        tvIngredientName = findViewById(R.id.tvIngredientName);
        tvRecipesUsed = findViewById(R.id.tvRecipesUsed);
        tvEnergy = findViewById(R.id.tvEnergy);
        tvFat = findViewById(R.id.tvFat);
        tvSaturatedFat = findViewById(R.id.tvSaturatedFat);
        tvCarbs = findViewById(R.id.tvCarbs);
        tvSugars = findViewById(R.id.tvSugars);
        tvProtein = findViewById(R.id.tvProtein);
        tvSalt = findViewById(R.id.tvSalt);
        btnAssign = findViewById(R.id.btnAssignToRecipe);
        btnModify = findViewById(R.id.btnModifyQuantity);
        btnRemove = findViewById(R.id.btnRemoveFromRecipe);
        btnDelete = findViewById(R.id.btnDeleteIngredient);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnAssign.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddIngredientToRecipeActivity.class);
            intent.putExtra("INGREDIENT_ID", ingredientId);
            startActivity(intent);
        });
        btnModify.setOnClickListener(v -> {
            Intent intent = new Intent(this, ModifyIngredientQuantityActivity.class);
            intent.putExtra("INGREDIENT_ID", ingredientId);
            startActivity(intent);
        });
        btnRemove.setOnClickListener(v -> {
            Intent intent = new Intent(this, DeleteIngredientFromRecipeActivity.class);
            intent.putExtra("INGREDIENT_ID", ingredientId);
            startActivity(intent);
        });
        btnDelete.setOnClickListener(v -> confirmDeleteIngredient());
    }

    private void loadIngredientDetails() {
        new Thread(() -> {
            currentIngredient = ingredientDao.getById(ingredientId);
            List<RecipeDao.RecipeIngredientRow> recipeRows = recipeDao.getRecipesForIngredient(ingredientId);

            runOnUiThread(() -> {
                if (currentIngredient == null) {
                    Toast.makeText(this, "Ingredient has been deleted.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                tvIngredientName.setText(currentIngredient.getName());
                displayNutrition(currentIngredient.getNutritionFact());
                displayRecipesUsed(recipeRows);
            });
        }).start();
    }

    private void displayNutrition(NutritionFact nf) {
        if (nf == null) return;
        tvEnergy.setText(String.format(Locale.getDefault(), "Total energy: %.0f kcal", nf.getEnergyKcal()));
        tvFat.setText(String.format(Locale.getDefault(), "Fat: %.1f g", nf.getFat()));
        tvSaturatedFat.setText(String.format(Locale.getDefault(), "  of which saturates: %.1f g", nf.getSaturatedFat()));
        tvCarbs.setText(String.format(Locale.getDefault(), "Carbohydrates: %.1f g", nf.getCarbs()));
        tvSugars.setText(String.format(Locale.getDefault(), "  of which sugars: %.1f g", nf.getSugars()));
        tvProtein.setText(String.format(Locale.getDefault(), "Proteins: %.1f g", nf.getProtein()));
        tvSalt.setText(String.format(Locale.getDefault(), "Salt: %.1f g", nf.getSalt()));
    }

    private void displayRecipesUsed(List<RecipeDao.RecipeIngredientRow> recipeRows) {
        if (recipeRows == null || recipeRows.isEmpty()) {
            tvRecipesUsed.setText("None");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (RecipeDao.RecipeIngredientRow row : recipeRows) {
            sb.append("- ")
                    .append(row.recipe.getName())
                    .append(String.format(Locale.getDefault(), " (%.1f %s)", row.quantityInGrams, row.unit))
                    .append("\n");
        }
        tvRecipesUsed.setText(sb.toString().trim());
    }

    private void confirmDeleteIngredient() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage("Are you sure you want to permanently delete '" + currentIngredient.getName() + "'?\nThis will also remove it from all recipes.")
                .setPositiveButton("Delete", (dialog, which) -> deleteIngredient())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteIngredient() {
        new Thread(() -> {
            int rowsDeleted = ingredientDao.deleteById(ingredientId);
            runOnUiThread(() -> {
                if (rowsDeleted > 0) {
                    Toast.makeText(this, "Ingredient deleted successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Error deleting ingredient.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ingredientDao != null) ingredientDao.close();
        if (recipeDao != null) recipeDao.close();
    }
}