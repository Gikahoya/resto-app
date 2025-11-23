package com.utaste.app.chef;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.utaste.R;
import com.utaste.data.sqlite.IngredientDao;
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.Recipe;
import com.utaste.service.RecipeIngredientService;

import java.util.ArrayList;
import java.util.List;

/**
 * Activité permettant de lier un ingrédient existant à une recette existante.
 * Elle utilise le RecipeIngredientService pour gérer la logique métier.
 */
public class AddIngredientToRecipeActivity extends AppCompatActivity {

    // --- Éléments de l'interface utilisateur (Vues) ---
    private ImageButton btnBack;            // Bouton retour
    private Spinner spRecipe;               // Liste déroulante des recettes
    private AutoCompleteTextView actvIngredient; // Champ de recherche d'ingrédient (avec suggestions)
    private EditText etQuantity;            // Champ pour saisir la quantité
    private Spinner spUnit;                 // Liste déroulante des unités (g, kg, L...)
    private Button btnSave;                 // Bouton de validation

    // --- Services et Accès aux Données ---
    private RecipeIngredientService recipeIngredientService; // Service principal pour la création du lien
    private RecipeDao recipeDao;           // Pour récupérer la liste des recettes
    private IngredientDao ingredientDao;   // Pour récupérer la liste des ingrédients (pour l'autocomplétion)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient_to_recipe);

        // 1. Initialisation des dépendances (Service et DAOs)
        recipeIngredientService = new RecipeIngredientService(this);
        recipeDao = new RecipeDao(this);
        ingredientDao = new IngredientDao(this);

        // 2. Liaison des vues XML aux variables Java
        initViews();

        // 3. Configuration des listes déroulantes (Spinners et AutoComplete)
        setupRecipeSpinner();
        setupIngredientAutoComplete();
        setupUnitSpinner();

        // 4. Gestion des événements (Clics boutons)
        setupListeners();
    }

    /**
     * Récupère les références des composants graphiques définis dans le XML.
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        spRecipe = findViewById(R.id.spRecipe);
        actvIngredient = findViewById(R.id.actvIngredient);
        etQuantity = findViewById(R.id.etQuantity);
        spUnit = findViewById(R.id.spUnit);
        btnSave = findViewById(R.id.btnSave);
    }

    /**
     * Remplit le Spinner avec la liste des noms de recettes disponibles en BDD.
     */
    private void setupRecipeSpinner() {
        List<Recipe> recipes = recipeDao.getAll();
        List<String> recipeNames = new ArrayList<>();

        // Extraction uniquement des noms pour l'affichage
        for (Recipe r : recipes) {
            recipeNames.add(r.getName());
        }

        // Création de l'adaptateur pour afficher les chaînes de caractères
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, recipeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRecipe.setAdapter(adapter);
    }

    /**
     * Configure l'autocomplétion pour le champ ingrédient.
     * Permet à l'utilisateur de taper "Fa" et de voir "Farine" apparaitre.
     */
    private void setupIngredientAutoComplete() {
        List<Ingredient> ingredients = ingredientDao.getAll();
        List<String> ingredientNames = new ArrayList<>();

        for (Ingredient i : ingredients) {
            ingredientNames.add(i.getName());
        }

        // Utilisation d'un layout simple pour la liste de suggestions
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ingredientNames);
        actvIngredient.setAdapter(adapter);
    }

    /**
     * Remplit le Spinner des unités avec les valeurs de l'Enum Ingredient.Unit.
     */
    private void setupUnitSpinner() {
        ArrayAdapter<Ingredient.Unit> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Ingredient.Unit.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnit.setAdapter(adapter);
    }

    /**
     * Définit les actions lors du clic sur les boutons.
     */
    private void setupListeners() {
        // Retour en arrière
        btnBack.setOnClickListener(v -> finish());

        // Sauvegarde
        btnSave.setOnClickListener(v -> saveLink());
    }

    /**
     * Logique de validation et de sauvegarde du lien Recette-Ingrédient.
     */
    private void saveLink() {
        // Récupération des textes saisis
        String ingredientName = actvIngredient.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();

        // --- Validations ---

        // Vérification qu'une recette est bien sélectionnée
        if (spRecipe.getSelectedItem() == null) {
            Toast.makeText(this, "Please create a recipe first!", Toast.LENGTH_SHORT).show();
            return;
        }
        String recipeName = spRecipe.getSelectedItem().toString();

        // Vérification que le nom de l'ingrédient n'est pas vide
        if (TextUtils.isEmpty(ingredientName)) {
            actvIngredient.setError("Required"); // "Requis"
            return;
        }

        // Vérification que la quantité n'est pas vide
        if (TextUtils.isEmpty(quantityStr)) {
            etQuantity.setError("Required");
            return;
        }

        // Conversion de la quantité (String -> double) en gérant les erreurs (virgules, texte...)
        double quantity;
        try {
            // On remplace la virgule par un point pour le format numérique standard
            quantity = Double.parseDouble(quantityStr.replace(",", "."));
        } catch (NumberFormatException e) {
            etQuantity.setError("Invalid number");
            return;
        }

        // Récupération de l'unité choisie (avec valeur par défaut PIECE si null)
        Ingredient.Unit selectedUnit = (Ingredient.Unit) spUnit.getSelectedItem();
        String unitString = (selectedUnit != null) ? selectedUnit.name() : Ingredient.Unit.PIECE.name();

        // --- Appel au Service ---
        // On délègue la recherche des IDs et l'insertion SQL au Service
        boolean success = recipeIngredientService.addIngredientToRecipeByNames(
                recipeName,
                ingredientName,
                quantity,
                unitString
        );

        // --- Feedback Utilisateur ---
        if (success) {
            Toast.makeText(this, "Ingredient added successfully!", Toast.LENGTH_SHORT).show();

            // On vide les champs pour permettre d'ajouter rapidement un autre ingrédient
            actvIngredient.setText("");
            etQuantity.setText("");
            actvIngredient.requestFocus(); // Remet le focus clavier sur le champ ingrédient
        } else {
            // Erreur généralement due à un ingrédient mal orthographié ou inexistant
            Toast.makeText(this, "Error: Ingredient or Recipe not found.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Fermeture propre des connexions à la base de données pour éviter les fuites de mémoire
        if (recipeIngredientService != null) recipeIngredientService.close();
        if (recipeDao != null) recipeDao.close();
        if (ingredientDao != null) ingredientDao.close();
    }
}