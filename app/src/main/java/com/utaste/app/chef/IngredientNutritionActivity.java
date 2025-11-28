package com.utaste.app.chef;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.domain.recipe.NutritionFact;
import com.utaste.domain.recipe.OpenFoodFactsClient;

import java.util.Locale;

/**
 * Écran qui permet au chef de saisir un code-barres / QR code
 * d’un ingrédient et d’afficher ses infos nutritionnelles à
 * partir d’OpenFoodFacts.
 */
public class IngredientNutritionActivity extends AppCompatActivity {

    private EditText etBarcode;
    private Button btnFetch;
    private TextView tvCarbs, tvProtein, tvFat, tvFiber, tvSalt, tvEnergy;
    private ImageButton btnBack;

    private OpenFoodFactsClient offClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients_nutrition_facts);

        // Bind UI
        etBarcode = findViewById(R.id.etBarcode);
        btnFetch  = findViewById(R.id.btnFetch);
        tvCarbs   = findViewById(R.id.tvCarbs);
        tvProtein = findViewById(R.id.tvProtein);
        tvFat     = findViewById(R.id.tvFat);
        tvFiber   = findViewById(R.id.tvFiber);
        tvSalt    = findViewById(R.id.tvSalt);
        tvEnergy  = findViewById(R.id.tvEnergy);
        btnBack   = findViewById(R.id.btnBack);

        offClient = new OpenFoodFactsClient();

        btnBack.setOnClickListener(v -> finish());
        btnFetch.setOnClickListener(v -> onFetchClicked());
    }

    private void onFetchClicked() {
        final String barcode = etBarcode.getText().toString().trim();

        if (barcode.isEmpty()) {
            etBarcode.setError("Barcode / QR code is required");
            etBarcode.requestFocus();
            return;
        }

        btnFetch.setEnabled(false);
        btnFetch.setText("Loading...");

        // Appel réseau dans un thread séparé (car fetchNutritionForBarcode est synchrone)
        new Thread(() -> {
            NutritionFact nf = null;
            try {
                nf = offClient.fetchNutritionForBarcode(barcode);
            } catch (Exception e) {
                e.printStackTrace();
            }

            final NutritionFact result = nf;

            runOnUiThread(() -> {
                btnFetch.setEnabled(true);
                btnFetch.setText("Fetch nutrition facts");

                if (result == null) {
                    Toast.makeText(
                            this,
                            "Product not found on OpenFoodFacts.",
                            Toast.LENGTH_SHORT
                    ).show();
                    resetDisplay();
                    return;
                }

                showNutrition(result);
            });
        }).start();
    }

    private void resetDisplay() {
        tvCarbs.setText("Carbohydrates: 0.0 g");
        tvProtein.setText("Proteins: 0.0 g");
        tvFat.setText("Fat: 0.0 g");
        tvFiber.setText("Fiber: 0.0 g");
        tvSalt.setText("Salt: 0.0 g");
        tvEnergy.setText("Total energy: 0 kcal");
    }

    private void showNutrition(NutritionFact nf) {
        tvCarbs.setText(String.format(
                Locale.getDefault(),
                "Carbohydrates: %.1f g",
                nf.getCarbs()
        ));
        tvProtein.setText(String.format(
                Locale.getDefault(),
                "Proteins: %.1f g",
                nf.getProtein()
        ));
        tvFat.setText(String.format(
                Locale.getDefault(),
                "Fat: %.1f g",
                nf.getFat()
        ));
        tvFiber.setText(String.format(
                Locale.getDefault(),
                "Fiber: %.1f g",
                nf.getFiber()
        ));
        tvSalt.setText(String.format(
                Locale.getDefault(),
                "Salt: %.1f g",
                nf.getSalt()
        ));
        tvEnergy.setText(String.format(
                Locale.getDefault(),
                "Total energy: %.0f kcal",
                nf.getEnergyKcal()
        ));
    }
}
