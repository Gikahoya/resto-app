package com.utaste.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.utaste.R;
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.data.sqlite.SaleDao;
import com.utaste.domain.recipe.Recipe;
import com.utaste.domain.sale.Sale;

import java.util.List;

public class RegisterSaleActivity extends AppCompatActivity {

    private Spinner spinnerRecipes;
    private RatingBar ratingBarSale;
    private EditText editTextAppreciation;
    private Button btnSaveSale;
    private SaleDao saleDao;
    private RecipeDao recipeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_sale);

        spinnerRecipes = findViewById(R.id.spinnerRecipes);
        ratingBarSale = findViewById(R.id.ratingBarSale);
        editTextAppreciation = findViewById(R.id.editTextAppreciation);
        btnSaveSale = findViewById(R.id.btnSaveSale);

        saleDao = new SaleDao(this);
        recipeDao = new RecipeDao(this);
        loadRecipesIntoSpinner();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSaveSale.setOnClickListener(v -> {
            Object selectedItem = spinnerRecipes.getSelectedItem();
            if (selectedItem == null) {
                Toast.makeText(this, "No recipe selected", Toast.LENGTH_SHORT).show();
                return;
            }

            Recipe selectedRecipe = (Recipe) selectedItem;
            float rating = ratingBarSale.getRating();
            String appreciation = editTextAppreciation.getText().toString();

            if (rating < 1) {
                Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
            } else {
                Sale sale = new Sale();
                sale.setRecipeId(selectedRecipe.getId());
                sale.setRating((int) rating);
                sale.setAppreciation(appreciation);
                sale.setTimestamp(System.currentTimeMillis());

                saleDao.insertSale(sale);
                Toast.makeText(this, "Sale registered successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadRecipesIntoSpinner() {
        List<Recipe> recipes = recipeDao.getAll();
        RecipeSpinnerAdapter adapter = new RecipeSpinnerAdapter(this, recipes);
        spinnerRecipes.setAdapter(adapter);
    }

}