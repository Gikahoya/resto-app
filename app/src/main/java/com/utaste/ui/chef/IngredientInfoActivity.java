package com.utaste.ui.chef;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.data.sqlite.IngredientDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.NutritionFact;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IngredientInfoActivity extends AppCompatActivity {

    public static final String EXTRA_INGREDIENT_ID = "INGREDIENT_ID";

    private TextView tvIngredientName;
    private ListView lvNutritionFacts;
    private IngredientDao ingredientDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_info);

        tvIngredientName = findViewById(R.id.tvIngredientName);
        lvNutritionFacts = findViewById(R.id.lvNutritionFacts);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ingredientDao = new IngredientDao(this);

        int ingredientId = getIntent().getIntExtra(EXTRA_INGREDIENT_ID, -1);

        if (ingredientId == -1) {
            Toast.makeText(this, "Erreur: ID d'ingrédient manquant.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadAndDisplayIngredient(ingredientId);
    }

    private void loadAndDisplayIngredient(int id) {
        // Le DAO doit être appelé en arrière-plan, mais pour simplifier ici on le fait directement.
        // Pour une application de production, utiliser un Executor.
        Ingredient ingredient = ingredientDao.getIngredientById(id);

        if (ingredient == null) {
            Toast.makeText(this, "Ingrédient non trouvé.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvIngredientName.setText(ingredient.getName());

        NutritionFact nf = ingredient.getNutritionFact();
        List<String> displayList = new ArrayList<>();

        if (nf != null) {
            // Formater les données comme demandé
            displayList.add(String.format(Locale.US, "carbohydrates for 100g: %.2fg", nf.getCarbsPer100g()));
            displayList.add(String.format(Locale.US, "fat for 100g: %.2fg", nf.getFatsPer100g()));
            displayList.add(String.format(Locale.US, "fibers for 100g: %.2fg", nf.getFibersPer100g()));
            displayList.add(String.format(Locale.US, "proteins for 100g: %.2fg", nf.getProteinsPer100g()));
            displayList.add(String.format(Locale.US, "salt for 100g: %.2fg", nf.getSaltPer100g()));
        } else {
            displayList.add("Aucune information nutritionnelle disponible pour cet ingrédient.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        lvNutritionFacts.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ingredientDao != null) {
            // Dans le DAO actuel, la DB est fermée après chaque opération.
            // Si cela change, appeler `ingredientDao.close()` ici.
        }
    }
}