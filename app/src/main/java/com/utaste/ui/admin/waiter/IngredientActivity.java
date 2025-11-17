package com.utaste.ui.admin.waiter;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.app.chef.OpenFoodFactsService;
import com.utaste.data.sqlite.IngredientDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.IngredientAdapter;
import com.utaste.domain.recipe.NutritionFact;
import com.utaste.ui.chef.IngredientInfoActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IngredientActivity extends AppCompatActivity {

    private ListView listView;
    private IngredientDao ingredientDao;
    private IngredientAdapter adapter;
    private final List<Ingredient> ingredientList = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private NutritionFact tempNutritionFact = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient);

        ingredientDao = new IngredientDao(this);
        listView = findViewById(R.id.listViewIngredients);

        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("Manage All Ingredients");

        // ===== ADAPTER MIS À JOUR =====
        adapter = new IngredientAdapter(this, ingredientList, new IngredientAdapter.Listener() {
            @Override
            public void onEdit(Ingredient ingredient) {
                showEditIngredientDialog(ingredient);
            }

            @Override
            public void onDelete(Ingredient ingredient) {
                confirmDelete(ingredient);
            }

            // IMPLÉMENTATION DE LA NOUVELLE MÉTHODE
            @Override
            public void onShowInfo(Ingredient ingredient) {
                if (ingredient.getNutritionFact() != null) {
                    Intent intent = new Intent(IngredientActivity.this, IngredientInfoActivity.class);
                    intent.putExtra(IngredientInfoActivity.EXTRA_INGREDIENT_ID, ingredient.getId());
                    startActivity(intent);
                } else {
                    toast("No nutritional facts available for this ingredient.");
                }
            }
        });
        // ================================

        listView.setAdapter(adapter);

        Button btnAdd = findViewById(R.id.btnAddIngredient);
        btnAdd.setText("Add New Ingredient");
        btnAdd.setOnClickListener(v -> showAddIngredientDialog());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // ON SUPPRIME L'ANCIEN LISTENER QUI NE MARCHAIT PAS
        // listView.setOnItemClickListener(...);
    }

    // ... (Le reste de la classe : onResume, reload, etc. ne change pas)
    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        executor.execute(() -> {
            List<Ingredient> allIngredients = ingredientDao.getAllIngredients();
            runOnUiThread(() -> {
                ingredientList.clear();
                ingredientList.addAll(allIngredients);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void confirmDelete(Ingredient ingredient) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage("Are you sure you want to delete '" + ingredient.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    executor.execute(() -> {
                        int deletedRows = ingredientDao.deleteIngredient(ingredient.getId());
                        runOnUiThread(() -> {
                            if (deletedRows > 0) {
                                toast("Ingredient deleted");
                                reload();
                            } else {
                                toast("Deletion failed");
                            }
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddIngredientDialog() {
        tempNutritionFact = null;
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_ingredient, null);

        EditText edtName = dialogView.findViewById(R.id.edtName);
        EditText edtQrCode = dialogView.findViewById(R.id.edtQrCode);
        EditText edtAmount = dialogView.findViewById(R.id.edtAmount);
        Spinner spUnit = dialogView.findViewById(R.id.spUnit);

        spUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Ingredient.Unit.values()));

        Button btnFetch = dialogView.findViewById(R.id.btnFetchNutritionFacts);
        btnFetch.setOnClickListener(v -> fetchNutritionData(edtQrCode.getText().toString()));

        new AlertDialog.Builder(this)
                .setTitle("Add Ingredient")
                .setView(dialogView)
                .setPositiveButton("Add", (d, w) -> {
                    String name = edtName.getText().toString().trim();
                    String qrCode = edtQrCode.getText().toString().trim();
                    String amountStr = edtAmount.getText().toString().trim();

                    if (name.isEmpty()) {
                        toast("Name is required.");
                        return;
                    }
                    double amount = 0;
                    try {
                        if (!amountStr.isEmpty()) amount = Double.parseDouble(amountStr);
                    } catch (NumberFormatException e) {
                        toast("Invalid quantity.");
                        return;
                    }

                    Ingredient ing = new Ingredient(name, qrCode, amount, (Ingredient.Unit) spUnit.getSelectedItem());
                    if (tempNutritionFact != null) {
                        ing.setNutritionFact(tempNutritionFact);
                    }

                    executor.execute(() -> {
                        long result = ingredientDao.insertIngredient(ing);
                        runOnUiThread(() -> {
                            if (result > -1) {
                                toast("Ingredient added.");
                                reload();
                            } else {
                                toast("Failed to add ingredient.");
                            }
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditIngredientDialog(Ingredient ingredient) {
        tempNutritionFact = ingredient.getNutritionFact();
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_ingredient, null);

        EditText edtName = dialogView.findViewById(R.id.edtName);
        EditText edtQrCode = dialogView.findViewById(R.id.edtQrCode);
        EditText edtAmount = dialogView.findViewById(R.id.edtAmount);
        Spinner spUnit = dialogView.findViewById(R.id.spUnit);

        spUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Ingredient.Unit.values()));

        edtName.setText(ingredient.getName());
        edtQrCode.setText(ingredient.getQrCode());
        edtAmount.setText(String.valueOf(ingredient.getAmount()));
        for (int i=0; i < spUnit.getCount(); i++) {
            if (spUnit.getItemAtPosition(i) == ingredient.getUnit()) {
                spUnit.setSelection(i);
                break;
            }
        }

        Button btnFetch = dialogView.findViewById(R.id.btnFetchNutritionFacts);
        btnFetch.setOnClickListener(v -> fetchNutritionData(edtQrCode.getText().toString()));

        new AlertDialog.Builder(this)
                .setTitle("Edit Ingredient")
                .setView(dialogView)
                .setPositiveButton("Save", (d, w) -> {
                    String newName = edtName.getText().toString().trim();
                    String newQrCode = edtQrCode.getText().toString().trim();
                    String newAmountStr = edtAmount.getText().toString().trim();

                    if (newName.isEmpty()) {
                        toast("Name cannot be empty.");
                        return;
                    }
                    double newAmount;
                    try { newAmount = Double.parseDouble(newAmountStr); } catch (NumberFormatException e) {
                        toast("Invalid quantity.");
                        return;
                    }

                    ingredient.setName(newName);
                    ingredient.setQrCode(newQrCode);
                    ingredient.setAmount(newAmount);
                    ingredient.setUnit((Ingredient.Unit) spUnit.getSelectedItem());
                    if (tempNutritionFact != null) {
                        ingredient.setNutritionFact(tempNutritionFact);
                    }

                    executor.execute(() -> {
                        int result = ingredientDao.updateIngredient(ingredient.getId(), ingredient);
                        runOnUiThread(() -> {
                            if (result > 0) {
                                toast("Ingredient updated.");
                                reload();
                            } else {
                                toast("Update failed.");
                            }
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void fetchNutritionData(String barcode) {
        if (barcode.isEmpty()) {
            toast("Please enter a barcode first.");
            return;
        }
        toast("Searching nutrition facts...");
        new OpenFoodFactsService().fetchNutritionFacts(barcode, new OpenFoodFactsService.NutritionFactCallback() {
            @Override
            public void onSuccess(NutritionFact nutritionFact) {
                tempNutritionFact = nutritionFact;
                toast("Nutrition facts found!");
            }
            @Override
            public void onError(String message) {
                tempNutritionFact = null;
                toast("Error: " + message);
            }
        });
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ingredientDao != null) {
            ingredientDao.close();
        }
    }
}