package com.utaste.ui.admin.waiter;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.utaste.R;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.IngredientDao;

import java.util.ArrayList;
import java.util.List;

public class IngredientActivity extends AppCompatActivity {

    private IngredientDao ingredientDao;
    private RecyclerView recyclerView;
    private IngredientAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_menu);

        ingredientDao = new IngredientDao(this);

        recyclerView = findViewById(R.id.recyclerIngredients);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new IngredientAdapter(new ArrayList<>(), new IngredientAdapter.Listener() {
            @Override public void onEdit(Ingredient ing) { showEditDialog(ing); }
            @Override public void onDelete(Ingredient ing) {
                int rows = ingredientDao.deleteIngredient(ing.getId());
                if (rows > 0) {
                    Toast.makeText(IngredientActivity.this, "Supprimé", Toast.LENGTH_SHORT).show();
                    reload();
                } else {
                    Toast.makeText(IngredientActivity.this, "Échec suppression", Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerView.setAdapter(adapter);

        Button btnAdd = findViewById(R.id.btnAddIngredient);
        btnAdd.setOnClickListener(v -> showAddDialog());

        reload();
    }

    private void reload() {
        List<Ingredient> list = ingredientDao.getAllIngredients();
        adapter.submit(list);
    }

    private void showAddDialog() {
        View dialog = LayoutInflater.from(this).inflate(R.layout.dialog_edit_ingredient, null, false);
        EditText edtName   = dialog.findViewById(R.id.edtName);
        EditText edtQr     = dialog.findViewById(R.id.edtQrCode);
        EditText edtAmount = dialog.findViewById(R.id.edtAmount);
        Spinner  spUnit    = dialog.findViewById(R.id.spUnit);

        ArrayAdapter<Ingredient.Unit> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, Ingredient.Unit.values());
        spUnit.setAdapter(unitAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Ajouter un ingrédient")
                .setView(dialog)
                .setPositiveButton("Ajouter", (d, w) -> {
                    String name = edtName.getText().toString().trim();
                    String qr = edtQr.getText().toString().trim();
                    double amount;
                    try { amount = Double.parseDouble(edtAmount.getText().toString().trim()); }
                    catch (Exception e) { amount = -1; }

                    Ingredient.Unit unit = (Ingredient.Unit) spUnit.getSelectedItem();

                    if (name.isEmpty() || amount < 0) {
                        Toast.makeText(this, "Valeurs invalides", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Ingredient ing = new Ingredient(name, qr.isEmpty() ? null : qr, amount, unit);
                    long id = ingredientDao.insertIngredient(ing);
                    if (id > 0) {
                        Toast.makeText(this, "Ajouté", Toast.LENGTH_SHORT).show();
                        reload();
                    } else {
                        Toast.makeText(this, "Échec insertion", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showEditDialog(Ingredient ing) {
        View dialog = LayoutInflater.from(this).inflate(R.layout.dialog_edit_ingredient, null, false);
        EditText edtName   = dialog.findViewById(R.id.edtName);
        EditText edtQr     = dialog.findViewById(R.id.edtQrCode);
        EditText edtAmount = dialog.findViewById(R.id.edtAmount);
        Spinner  spUnit    = dialog.findViewById(R.id.spUnit);

        edtName.setText(ing.getName());
        edtQr.setText(ing.getQrCode());
        edtAmount.setText(String.valueOf(ing.getAmount()));

        ArrayAdapter<Ingredient.Unit> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, Ingredient.Unit.values());
        spUnit.setAdapter(unitAdapter);
        spUnit.setSelection(ing.getUnit() != null ? ing.getUnit().ordinal() : 0);

        new AlertDialog.Builder(this)
                .setTitle("Modifier l'ingrédient")
                .setView(dialog)
                .setPositiveButton("Enregistrer", (d, w) -> {
                    String name = edtName.getText().toString().trim();
                    String qr = edtQr.getText().toString().trim();
                    double amount;
                    try { amount = Double.parseDouble(edtAmount.getText().toString().trim()); }
                    catch (Exception e) { amount = -1; }

                    Ingredient.Unit unit = (Ingredient.Unit) spUnit.getSelectedItem();

                    if (name.isEmpty() || amount < 0) {
                        Toast.makeText(this, "Valeurs invalides", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ing.setName(name);
                    ing.setQrCode(qr.isEmpty() ? null : qr);
                    ing.setAmount(amount);
                    ing.setUnit(unit);

                    int rows = ingredientDao.updateIngredient(ing.getId(), ing);
                    if (rows > 0) {
                        Toast.makeText(this, "Modifié", Toast.LENGTH_SHORT).show();
                        reload();
                    } else {
                        Toast.makeText(this, "Échec mise à jour", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private static class IngredientAdapter extends RecyclerView.Adapter<IngredientVH> {
        interface Listener {
            void onEdit(Ingredient ing);
            void onDelete(Ingredient ing);
        }

        private final List<Ingredient> data;
        private final Listener listener;

        IngredientAdapter(List<Ingredient> data, Listener listener) {
            this.data = data;
            this.listener = listener;
        }

        void submit(List<Ingredient> newData) {
            data.clear();
            data.addAll(newData);
            notifyDataSetChanged();
        }

        @Override public IngredientVH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient, parent, false);
            return new IngredientVH(v);
        }

        @Override public void onBindViewHolder(IngredientVH h, int pos) {
            Ingredient ing = data.get(pos);
            h.txtName.setText(ing.getName());
            h.txtQuantity.setText(ing.getDisplayQuantity());
            h.txtQr.setText(ing.getQrCode() == null ? "—" : ing.getQrCode());

            h.btnEdit.setOnClickListener(v -> listener.onEdit(ing));
            h.btnDelete.setOnClickListener(v -> listener.onDelete(ing));
        }

        @Override public int getItemCount() { return data.size(); }
    }

    private static class IngredientVH extends RecyclerView.ViewHolder {
        final android.widget.TextView txtName, txtQuantity, txtQr;
        final Button btnEdit, btnDelete;

        IngredientVH(View itemView) {
            super(itemView);
            txtName    = itemView.findViewById(R.id.txtName);
            txtQuantity= itemView.findViewById(R.id.txtQuantity);
            txtQr      = itemView.findViewById(R.id.txtQrCode);
            btnEdit    = itemView.findViewById(R.id.btnEdit);
            btnDelete  = itemView.findViewById(R.id.btnDelete);
        }
    }
}
