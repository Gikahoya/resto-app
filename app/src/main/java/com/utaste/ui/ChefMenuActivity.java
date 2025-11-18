package com.utaste.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.WelcomeActivity;
import com.utaste.app.chef.AddIngredientToRecipeActivity;
import com.utaste.app.chef.DeleteIngredientFromRecipeActivity;
import com.utaste.app.chef.IngredientNutritionActivity;
import com.utaste.app.chef.ModifyIngredientQuantityActivity;
import com.utaste.app.chef.RecipeCaloricBalanceActivity;
import com.utaste.data.sqlite.UserDao;
import com.utaste.ui.recipe.RecipeActivity;

public class ChefMenuActivity extends AppCompatActivity {

    private UserDao userDao;

    private Button changePwdBtn;
    private Button editRecipeBtn;
    private Button addIngredientBtn;
    private Button ingredientQtyBtn;
    private Button deleteIngredientBtn;
    private Button ingredientsInfoBtn;
    private Button caloricBalanceBtn;
    private Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_menu);

        userDao = new UserDao(this);

        changePwdBtn        = findViewById(R.id.change_pwd);
        editRecipeBtn       = findViewById(R.id.edit_recipe);
        addIngredientBtn    = findViewById(R.id.add_ingredient);        // "Add Ingredient to Recipe"
        ingredientQtyBtn    = findViewById(R.id.ingredient_quantity);   // "Modify Ingredient Quantity to Recipe"
        deleteIngredientBtn = findViewById(R.id.delete_ingredient);
        ingredientsInfoBtn  = findViewById(R.id.ingredients_info);
        caloricBalanceBtn   = findViewById(R.id.caloric_balance);
        logoutBtn           = findViewById(R.id.logout);

        // Logout
        logoutBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });

        // Create / modify / delete recipes
        editRecipeBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RecipeActivity.class)));

        // Change password
        changePwdBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));

        // 3e bouton : Add ingredient to recipe
        addIngredientBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AddIngredientToRecipeActivity.class)));

        // 4e bouton : Modify ingredient quantity (pas encore implémenté)
        ingredientQtyBtn.setOnClickListener(v ->
                Toast.makeText(this,
                        "Modify ingredient quantity — coming soon",
                        Toast.LENGTH_SHORT).show());

        // Delete ingredient from recipe (pas encore implémenté)
        deleteIngredientBtn.setOnClickListener(v ->
                Toast.makeText(this,
                        "Delete ingredient from recipe — coming soon",
                        Toast.LENGTH_SHORT).show());

        // Ingredient nutritional facts (OpenFoodFacts)
        ingredientsInfoBtn.setOnClickListener(v ->
                startActivity(new Intent(this, IngredientNutritionActivity.class)));
        // ➜ Modifier la quantité d'un ingrédient d'une recette
        ingredientQtyBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ModifyIngredientQuantityActivity.class)));

        // ➜ Supprimer un ingrédient d'une recette
        deleteIngredientBtn.setOnClickListener(v ->
                startActivity(new Intent(this, DeleteIngredientFromRecipeActivity.class)));

        // Recipe caloric balance
        caloricBalanceBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RecipeCaloricBalanceActivity.class)));
    }
}
