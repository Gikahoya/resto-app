package com.utaste.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.WelcomeActivity;
import com.utaste.app.chef.SalesSummaryActivity;
import com.utaste.ui.waiter.RecipeListActivity;

public class WaiterMenuActivity extends AppCompatActivity {

    private Button changePwdBtn;
    private Button recipesBtn;
    private Button registerSaleBtn;
    private Button salesReportBtn;
    private Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiter_menu);

        changePwdBtn    = findViewById(R.id.change_pwd);
        recipesBtn      = findViewById(R.id.recipes);
        registerSaleBtn = findViewById(R.id.register_sale);
        salesReportBtn  = findViewById(R.id.sales_report);
        logoutBtn       = findViewById(R.id.logout);

        // Change password (si tu as déjà ChangePasswordActivity dans com.utaste.ui)
        changePwdBtn.setOnClickListener(v -> {
            Intent intent = new Intent(WaiterMenuActivity.this, ChangePasswordActivity.class);

            // On récupère le nom d'utilisateur qui a été passé à CETTE activité lors du login
            String username = getIntent().getStringExtra("username");

            // On le passe à l'activité suivante pour qu'elle sache QUI change son mot de passe
            intent.putExtra("username", username);

            startActivity(intent);
        });
        // Recipes (même écran que pour le chef)
        recipesBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RecipeListActivity.class)));

        // Enregistrer une vente
        registerSaleBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterSaleActivity.class)));

        // 🧾 Sales Report = Bilan des ventes
        salesReportBtn.setOnClickListener(v ->
                startActivity(new Intent(this, SalesSummaryActivity.class)));

        // Logout
        logoutBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });

    }
}
