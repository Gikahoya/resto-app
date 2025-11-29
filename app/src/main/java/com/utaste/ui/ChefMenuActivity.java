package com.utaste.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.WelcomeActivity;
import com.utaste.app.chef.IngredientListActivity;
import com.utaste.app.chef.IngredientNutritionActivity;
import com.utaste.app.chef.RecipeCaloricBalanceActivity;
import com.utaste.app.chef.ManageRecipeActivity;

public class ChefMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_menu);

        Button manageRecipesBtn = findViewById(R.id.manage_recipes_btn);
        Button manageIngredientsBtn = findViewById(R.id.manage_ingredients_btn);
        Button ingredientsInfoBtn = findViewById(R.id.ingredients_info_btn);
        Button caloricBalanceBtn = findViewById(R.id.caloric_balance_btn);
        Button changePwdBtn = findViewById(R.id.change_pwd_btn);
        Button logoutBtn = findViewById(R.id.logout_btn);

        manageRecipesBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ManageRecipeActivity.class)));

        manageIngredientsBtn.setOnClickListener(v ->
                startActivity(new Intent(this, IngredientListActivity.class)));

        ingredientsInfoBtn.setOnClickListener(v ->
                startActivity(new Intent(this, IngredientNutritionActivity.class)));

        caloricBalanceBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RecipeCaloricBalanceActivity.class)));

        changePwdBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ChefMenuActivity.this, ChangePasswordActivity.class);

            // On récupère le nom d'utilisateur qui a été passé à CETTE activité lors du login
            String username = getIntent().getStringExtra("username");

            // On le passe à l'activité suivante pour qu'elle sache QUI change son mot de passe
            intent.putExtra("username", username);

            startActivity(intent);
        });

        logoutBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });
    }
}