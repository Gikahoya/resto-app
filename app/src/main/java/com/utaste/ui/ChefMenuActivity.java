package com.utaste.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.WelcomeActivity;
import com.utaste.data.sqlite.UserDao;
import com.utaste.ui.admin.waiter.IngredientActivity;
import com.utaste.ui.recipe.RecipeActivity;

/**
 * ChefMenuActivity .
 * Branche tous les boutons du layout:
 * - change_pwd
 * - add_ingredient
 * - ingredient_quantity
 * - ingredients_info
 * - edit_recipe
 * - logout
 */
public class ChefMenuActivity extends AppCompatActivity {

    private UserDao userDao;

    private Button changePwdBtn, editRecipeBtn, manageIngredientsBtn,
            caloricBalanceBtn, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_menu);

        userDao = new UserDao(this);

        // Bind des boutons restants
        changePwdBtn        = findViewById(R.id.change_pwd);
        editRecipeBtn       = findViewById(R.id.edit_recipe);
        manageIngredientsBtn= findViewById(R.id.manage_ingredients_btn); // Nouvel ID
        caloricBalanceBtn   = findViewById(R.id.caloric_balance);
        logoutBtn           = findViewById(R.id.logout);

        // ===== Actions =====
        logoutBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });

        editRecipeBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, RecipeActivity.class));
        });

        changePwdBtn.setOnClickListener(v -> {
            String username = getIntent().getStringExtra("username");
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        manageIngredientsBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, com.utaste.ui.admin.waiter.IngredientActivity.class));
        });

        // Placeholder pour la feature pas encore faite
        caloricBalanceBtn.setOnClickListener(v -> toast("Caloric balance — coming soon"));
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}
