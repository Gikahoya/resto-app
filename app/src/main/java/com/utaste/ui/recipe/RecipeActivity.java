package com.utaste.ui.recipe;   é

import android.os.Bundle;                // gère le cycle de vie de l’activité
import android.text.TextUtils;           // permet de vérifier si un texte est vide
import android.view.View;                // utilisé pour les boutons (onClick)
import android.widget.EditText;          // champs de saisie
import android.widget.Toast;             // messages courts affichés à l’écran

import androidx.appcompat.app.AppCompatActivity;  // base pour une activité Android

import com.utaste.R;                     // accès aux ressources du projet (layout, id…)
import com.utaste.data.sqlite.RecipeDao; // accès à la base de données des recettes


public class RecipeActivity extends AppCompatActivity {

    // === Déclaration des champs du formulaire ===
    private EditText edtName;          // pour le nom de la recette
    private EditText edtDescription;   // pour la description
    private EditText edtImagePath;     // pour le lien ou le chemin d’image

    // Accès à la base SQLite
    private RecipeDao dao;

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
     * Affiche un petit message à l’écran.
     * (utilisé pour informer l’utilisateur après chaque action)
     */
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
