package com.utaste.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.WelcomeActivity;
import com.utaste.app.chef.AddIngredientToRecipeActivity;
import com.utaste.app.chef.RecipeCaloricBalanceActivity;
import com.utaste.data.sqlite.UserDao;
import com.utaste.ui.admin.waiter.IngredientActivity;
import com.utaste.ui.recipe.RecipeActivity;

public class ChefMenuActivity extends AppCompatActivity {

    private UserDao userDao;

    private Button changePwdBtn, editRecipeBtn, addIngredientBtn,
            ingredientQtyBtn, deleteIngredientBtn, ingredientsInfoBtn,
            caloricBalanceBtn, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_menu);

        userDao = new UserDao(this);

        changePwdBtn        = findViewById(R.id.change_pwd);
        editRecipeBtn       = findViewById(R.id.edit_recipe);
        addIngredientBtn    = findViewById(R.id.add_ingredient);
        ingredientQtyBtn    = findViewById(R.id.ingredient_quantity); // renommé en "Add ingredient to recipe"
        deleteIngredientBtn = findViewById(R.id.delete_ingredient);
        ingredientsInfoBtn  = findViewById(R.id.ingredients_info);
        caloricBalanceBtn   = findViewById(R.id.caloric_balance);
        logoutBtn           = findViewById(R.id.logout);

        // Logout
        logoutBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });

        // Modifier / créer / supprimer des recettes
        editRecipeBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RecipeActivity.class)));

        // Changer le mot de passe
        changePwdBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));

        // Gérer la liste globale des ingrédients
        addIngredientBtn.setOnClickListener(v ->
                startActivity(new Intent(this, IngredientActivity.class)));

        // ➜ Ajouter un ingrédient à une RECETTE
        ingredientQtyBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AddIngredientToRecipeActivity.class)));

        // (optionnel) infos ingrédients – pas implémenté
        ingredientsInfoBtn.setOnClickListener(v ->
                toast("Ingredients info — coming soon"));

        // ➜ Bilan calorique des recettes
        caloricBalanceBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RecipeCaloricBalanceActivity.class)));

        // Delete (non implémenté)
        deleteIngredientBtn.setOnClickListener(v ->
                toast("Delete ingredient — coming soon"));
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}
