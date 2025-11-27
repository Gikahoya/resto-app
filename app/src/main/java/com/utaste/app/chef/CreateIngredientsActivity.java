package com.utaste.app.chef;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.data.sqlite.IngredientDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.NutritionFact;
import com.utaste.domain.recipe.OpenFoodFactsClient;
import com.utaste.ui.QrScannerActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateIngredientsActivity extends AppCompatActivity {

    // Éléments d'interface
    private EditText etName;
    private EditText etQrCode;
    private Button btnSave;
    private ImageButton btnScanQr;

    // Outils techniques
    private IngredientDao ingredientDao;
    private final OpenFoodFactsClient apiClient = new OpenFoodFactsClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> scannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String barcode = result.getData().getStringExtra(QrScannerActivity.EXTRA_QR_TEXT);
                    if (barcode != null) {
                        etQrCode.setText(barcode);
                        Toast.makeText(this, "Barcode captured!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ingredient);

        ingredientDao = new IngredientDao(this);
        initViews();

        btnSave.setOnClickListener(v -> attemptSaveIngredient());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnScanQr.setOnClickListener(v -> {
            Intent intent = new Intent(this, QrScannerActivity.class);
            scannerLauncher.launch(intent);
        });
    }

    private void initViews() {
        etName = findViewById(R.id.et_ingredient_name);
        etQrCode = findViewById(R.id.et_qr_code);
        btnSave = findViewById(R.id.btn_save);
        btnScanQr = findViewById(R.id.btn_scan_qr);
    }

    private void attemptSaveIngredient() {
        String name = etName.getText().toString().trim();
        String qrCode = etQrCode.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name required.");
            return;
        }

        if (TextUtils.isEmpty(qrCode)) {
            etQrCode.setError("The QR code is mandatory for retrieving nutritional information.");
            return;
        }

        btnSave.setEnabled(false);
        Toast.makeText(this, "Nutritiousness data retrieval...", Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            NutritionFact nutritionFact = null;
            try {
                nutritionFact = apiClient.fetchNutritionForBarcode(qrCode);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Ingredient ingredient = new Ingredient(name, qrCode);
            if (nutritionFact != null) {
                ingredient.setNutritionFact(nutritionFact);
            }

            long id = ingredientDao.insert(ingredient);

            NutritionFact finalNutritionFact = nutritionFact;
            mainHandler.post(() -> {
                btnSave.setEnabled(true);

                if (id != -1) {
                    String msg = "Ingredient created successfully !";
                    if (finalNutritionFact != null) {
                        msg += " (Nutritiousness data retrieved)";
                    } else {
                        msg += " (Nutritiousness data not available)";
                    }
                    Toast.makeText(CreateIngredientsActivity.this, msg, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(CreateIngredientsActivity.this, "Error creating ingredient", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ingredientDao != null) {
            ingredientDao.close();
        }
    }
}