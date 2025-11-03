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

    private Button changePwdBtn, editRecipeBtn, addIngredientBtn,
            ingredientQtyBtn, deleteIngredientBtn, ingredientsInfoBtn,
            caloricBalanceBtn, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_menu);

        userDao = new UserDao(this);

        // Bind des boutons (IDs déjà présents dans activity_chef_menu.xml)
        changePwdBtn        = findViewById(R.id.change_pwd);
        editRecipeBtn       = findViewById(R.id.edit_recipe);
        addIngredientBtn    = findViewById(R.id.add_ingredient);
        ingredientQtyBtn    = findViewById(R.id.ingredient_quantity);
        deleteIngredientBtn = findViewById(R.id.delete_ingredient);
        ingredientsInfoBtn  = findViewById(R.id.ingredients_info);
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
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });
        addIngredientBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, IngredientActivity.class));
        });
        //ingredientQtyBtn.setOnClickListener(v -> showUpdateProfileDialog());
        //ingredientsInfoBtn.setOnClickListener(v -> showResetDatabaseDialog());

        // Placeholders propres pour les features pas encore faites
        deleteIngredientBtn.setOnClickListener(v -> toast("Delete ingredient — coming soon"));
        caloricBalanceBtn.setOnClickListener(v -> toast("Caloric balance — coming soon"));
    }

    private LinearLayout verticalBox() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = (int) (16 * getResources().getDisplayMetrics().density);
        box.setPadding(p, p, p, p);
        return box;
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}
