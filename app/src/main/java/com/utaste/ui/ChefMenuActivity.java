package com.utaste.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.data.sqlite.RecipeActivity;

public class ChefMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_menu);
    }

    /**
     * Bouton "Change Password"
     */
    public void onChangePasswordClicked(View view) {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        startActivity(intent);
    }

    /**
     * Bouton "Create/Modify/Delete Recipe"
     * -> ouvre l'écran de gestion des recettes (RecipeActivity)
     */
    public void onCreateModifyDeleteRecipeClicked(View view) {
        Intent intent = new Intent(this, RecipeActivity.class);
        startActivity(intent);
    }

    /**
     * Bouton "Add Ingredient to Recipe"
     * -> réutilise le même écran RecipeActivity, où tu as le bouton "Add ingredient (QR)"
     */
    public void onAddIngredientToRecipeClicked(View view) {
        Intent intent = new Intent(this, RecipeActivity.class);
        startActivity(intent);
    }

    // les autres boutons (Modify quantity, Delete ingredient, etc.)
    // pourront être implémentés plus tard.
}
