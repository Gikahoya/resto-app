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

        editRecipeBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RecipeActivity.class)));

        changePwdBtn.setOnClickListener(v -> showChangeOwnPasswordDialog());
        addIngredientBtn.setOnClickListener(v -> showChangeUserPasswordDialog());
        ingredientQtyBtn.setOnClickListener(v -> showUpdateProfileDialog());
        ingredientsInfoBtn.setOnClickListener(v -> showResetDatabaseDialog());

        // Placeholders propres pour les features pas encore faites
        deleteIngredientBtn.setOnClickListener(v -> toast("Delete ingredient — coming soon"));
        caloricBalanceBtn.setOnClickListener(v -> toast("Caloric balance — coming soon"));
    }


    /** Change ton propre mot de passe  */
    private void showChangeOwnPasswordDialog() {
        LinearLayout box = verticalBox();

        EditText edtEmail = new EditText(this);
        edtEmail.setHint("Your email");
        edtEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        EditText edtNewPwd = new EditText(this);
        edtNewPwd.setHint("New password");
        edtNewPwd.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        String fromWelcome = getIntent().getStringExtra("username");
        if (!TextUtils.isEmpty(fromWelcome)) edtEmail.setText(fromWelcome);

        box.addView(edtEmail);
        box.addView(edtNewPwd);

        new AlertDialog.Builder(this)
                .setTitle("Change password (you)")
                .setView(box)
                .setPositiveButton("Save", (d, w) -> {
                    String email = edtEmail.getText().toString().trim();
                    String pwd   = edtNewPwd.getText().toString();

                    if (!isValidEmail(email))   { toast("Invalid email"); return; }
                    if (TextUtils.isEmpty(pwd)) { toast("New password required"); return; }
                    if (!userDao.exists(email)) { toast("User not found"); return; }

                    int rows = userDao.resetPassword(email, pwd);
                    toast(rows > 0 ? "Password updated" : "No change");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Change le mot de passe de n’importe quel utilisateur . */
    private void showChangeUserPasswordDialog() {
        LinearLayout box = verticalBox();

        EditText edtEmail = new EditText(this);
        edtEmail.setHint("User email");
        edtEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        EditText edtNewPwd = new EditText(this);
        edtNewPwd.setHint("New password");
        edtNewPwd.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        box.addView(edtEmail);
        box.addView(edtNewPwd);

        new AlertDialog.Builder(this)
                .setTitle("Change user password")
                .setView(box)
                .setPositiveButton("Save", (d, w) -> {
                    String email = edtEmail.getText().toString().trim();
                    String pwd   = edtNewPwd.getText().toString();

                    if (!isValidEmail(email))   { toast("Invalid email"); return; }
                    if (TextUtils.isEmpty(pwd)) { toast("New password required"); return; }
                    if (!userDao.exists(email)) { toast("User not found"); return; }

                    int rows = userDao.resetPassword(email, pwd);
                    toast(rows > 0 ? "Password updated" : "No change");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Update profile via email. */
    private void showUpdateProfileDialog() {
        LinearLayout box = verticalBox();

        EditText edtEmail = new EditText(this);
        edtEmail.setHint("User email");
        edtEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        EditText edtFirst = new EditText(this);
        edtFirst.setHint("First name (optional)");

        EditText edtLast  = new EditText(this);
        edtLast.setHint("Last name (optional)");

        String fromWelcome = getIntent().getStringExtra("username");
        if (!TextUtils.isEmpty(fromWelcome)) edtEmail.setText(fromWelcome);

        box.addView(edtEmail);
        box.addView(edtFirst);
        box.addView(edtLast);

        new AlertDialog.Builder(this)
                .setTitle("Update profile")
                .setView(box)
                .setPositiveButton("Save", (d, w) -> {
                    String email = edtEmail.getText().toString().trim();
                    String first = edtFirst.getText().toString().trim();
                    String last  = edtLast .getText().toString().trim();

                    if (!isValidEmail(email)) { toast("Invalid email"); return; }
                    if (TextUtils.isEmpty(first) && TextUtils.isEmpty(last)) {
                        toast("Nothing to update"); return;
                    }
                    if (!userDao.exists(email)) { toast("User not found"); return; }

                    int rows = userDao.updateProfile(email, first, last);
                    toast(rows > 0 ? "Profile updated" : "No change");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Reset DB : choix entre reset table users (drop+create) ou reset whole DB (fichier supprimé). */
    private void showResetDatabaseDialog() {
        String[] options = new String[]{
                "Reset ONLY 'users' table",
                "Reset WHOLE database (all tables)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Reset database")
                .setItems(options, (d, which) -> {
                    if (which == 0) {
                        userDao.resetUsersTable();
                        toast("Users table reset");
                    } else {
                        userDao.resetWholeDatabase(this);
                        toast("Whole database deleted");
                        // Retour propre à l’accueil
                        Intent i = new Intent(this, WelcomeActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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
