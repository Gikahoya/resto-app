package com.utaste.data.sqlite;

import android.content.Intent;
import android.os.Bundle;                // gère le cycle de vie de l’activité
import android.text.TextUtils;           // permet de vérifier si un texte est vide
import android.view.LayoutInflater;
import android.view.View;                // utilisé pour les boutons (onClick)
import android.widget.EditText;          // champs de saisie
import android.widget.Toast;             // messages courts affichés à l’écran

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;  // base pour une activité Android

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.utaste.R;                     // accès aux ressources du projet (layout, id…)
import com.utaste.app.chef.IngredientService;

public class RecipeActivity extends AppCompatActivity {

    // === Déclaration des champs du formulaire ===
    private EditText edtName;          // pour le nom de la recette
    private EditText edtDescription;   // pour la description
    private EditText edtImagePath;     // pour le lien ou le chemin d’image

    // Accès à la base SQLite
    private RecipeDao dao;

    // Service pour gérer les ingrédients via QR code
    private IngredientService ingredientService;

    // On garde le nom de la recette pour laquelle on ajoute l’ingrédient via QR
    private String currentRecipeNameForQr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe); // lie cette classe à son layout XML

        // Association des composants du layout avec les variables Java
        edtName        = findViewById(R.id.edtName);
        edtDescription = findViewById(R.id.edtDescription);
        edtImagePath   = findViewById(R.id.edtImagePath);

        // Initialisation du gestionnaire de base de données
        dao = new RecipeDao(this);

        // Service d’ingrédients (QR + enregistrement quantité)
        ingredientService = new IngredientService(this);
    }


    /**
     * Bouton "Create" :
     * Ajoute une recette si elle n’existe pas déjà dans la base.
     */
    public void onCreateRecipe(View v) {
        // Récupération du contenu des champs
        String name = edtName.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        String img  = edtImagePath.getText().toString().trim();

        // Vérifie que le nom n’est pas vide
        if (TextUtils.isEmpty(name)) {
            toast("Name is required");
            return;
        }

        // Tente d’insérer la recette dans la base
        long rowId = dao.insertIfAbsent(name, desc, img);

        // Retour utilisateur : succès ou déjà existante
        toast(rowId == -1 ? "Recipe already exists" : "Recipe created");
    }

    /**
     * Bouton "Update" :
     * Modifie la description et l’image d’une recette existante.
     */
    public void onUpdateRecipe(View v) {
        String name = edtName.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        String img  = edtImagePath.getText().toString().trim();

        // Validation : un nom est obligatoire
        if (TextUtils.isEmpty(name)) {
            toast("Name is required");
            return;
        }

        // Vérifie que la recette existe avant de la modifier
        if (!dao.exists(name)) {
            toast("Recipe not found");
            return;
        }

        // Met à jour la recette
        int rows = dao.updateByName(name, desc, img);

        // Informe l’utilisateur si la modification a réussi
        toast(rows > 0 ? "Recipe updated" : "No change");
    }

    /**
     * Bouton "Delete" :
     * Supprime une recette de la base en fonction de son nom.
     */
    public void onDeleteRecipe(View v) {
        String name = edtName.getText().toString().trim();

        // Validation : le champ ne doit pas être vide
        if (TextUtils.isEmpty(name)) {
            toast("Name is required");
            return;
        }

        // Suppression de la recette correspondante
        int rows = dao.deleteByName(name);

        // Retour utilisateur selon le résultat
        toast(rows > 0 ? "Recipe deleted" : "Recipe not found");
    }

    /**
     * Bouton "Add ingredient (QR)" :
     * scanne un QR pour ajouter un ingrédient avec titre + quantité.
     * (lié au bouton via android:onClick="onScanIngredientQr")
     */
    public void onScanIngredientQr(View v) {
        String name = edtName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            toast("Enter recipe name first");
            return;
        }

        if (!dao.exists(name)) {
            toast("Recipe not found. Create or save it first.");
            return;
        }

        // On garde le nom de la recette pour usage après le scan
        currentRecipeNameForQr = name;

        startQrScan();
    }

    private void startQrScan() {
        new IntentIntegrator(this)
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                .setPrompt("Scan the ingredient QR code")
                .setBeepEnabled(false)
                .setOrientationLocked(true)
                .initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String qrCodeValue = result.getContents();
                showAddIngredientDialog(qrCodeValue);
            } else {
                toast("No QR code scanned");
            }
        }
    }

    /**
     * Affiche un dialog pour saisir le titre + quantité après le scan.
     */
    private void showAddIngredientDialog(String qrCodeValue) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_ingredient_qr, null);

        EditText titleEdit = dialogView.findViewById(R.id.editIngredientTitle);
        EditText quantityEdit = dialogView.findViewById(R.id.editIngredientQuantity);

        new AlertDialog.Builder(this)
                .setTitle("Add ingredient")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = titleEdit.getText().toString().trim();
                    String quantityStr = quantityEdit.getText().toString().trim();

                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(quantityStr)) {
                        toast("Title and quantity are required");
                        return;
                    }

                    double quantity;
                    try {
                        quantity = Double.parseDouble(quantityStr);
                    } catch (NumberFormatException e) {
                        toast("Invalid quantity");
                        return;
                    }

                    boolean ok = ingredientService.addIngredientToRecipeFromQrByRecipeName(
                            currentRecipeNameForQr,
                            title,
                            qrCodeValue,
                            quantity,
                            null   // unit optionnelle, tu peux mettre "g" par ex.
                    );

                    if (ok) {
                        toast("Ingredient added");
                    } else {
                        toast("Error while saving ingredient");
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Affiche un petit message à l’écran.
     * (utilisé pour informer l’utilisateur après chaque action)
     */
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
