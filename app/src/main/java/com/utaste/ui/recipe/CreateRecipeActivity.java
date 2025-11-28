package com.utaste.ui.recipe;

import android.content.Intent; // ✅ AJOUT
import android.os.Bundle;import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView; // ✅ AJOUT
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.utaste.R;
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.domain.recipe.Recipe;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList; // ✅ AJOUT
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateRecipeActivity extends AppCompatActivity {

    private static final String PEXELS_API_KEY = "SSXpX9eI3YazHuoWxgA5mFEHSguIl04baBvbLOyNGo7vcCidyUND9uLX";   // API key for Pexels

    // Composants de l'interface
    private EditText edtName, edtDescription;
    private ImageView imgRecipePreview; // Remplacement de l'ancien EditText d'image
    private Button btnSearchImage;
    private long currentRecipeId = -1L; // ✅ AJOUT: Pour garder l'ID en mode édition

    // Données
    private RecipeDao dao;
    private String selectedImageUrl = null; // Pour stocker l'URL de l'image choisie

    // Outils pour les tâches en arrière-plan et le parsing
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        // Initialisation des vues
        edtName = findViewById(R.id.edtName);
        edtDescription = findViewById(R.id.edtDescription);
        imgRecipePreview = findViewById(R.id.imgRecipePreview);
        btnSearchImage = findViewById(R.id.btnSearchImage);

        // Initialisation des listeners
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSearchImage.setOnClickListener(v -> showImageSearchDialog());

        // Initialisation de la base de données
        dao = new RecipeDao(this);

        // 🛑 SUPPRESSION du listener qui charge automatiquement la recette par le nom.
        // Cela entre en conflit avec le mode édition.
        // edtName.setOnFocusChangeListener((v, hasFocus) -> { ... });

        // ✅ NOUVEAU : Vérifier si on est en mode édition
        checkForEditMode();
    }

    /**
     * ✅ NOUVELLE MÉTHODE
     * Vérifie si des données ont été passées via l'Intent (mode édition)
     * et pré-remplit le formulaire.
     */
    private void checkForEditMode() {
        Intent intent = getIntent();
        // On vérifie la présence d'un ID, signe d'une édition
        if (intent != null && intent.hasExtra("RECIPE_ID")) {
            // -- On est en mode ÉDITION --

            // 1. Récupérer les données de l'Intent
            currentRecipeId = intent.getLongExtra("RECIPE_ID", -1L);
            String name = intent.getStringExtra("RECIPE_NAME");
            String description = intent.getStringExtra("RECIPE_DESCRIPTION");
            String imagePath = intent.getStringExtra("RECIPE_IMAGE_PATH");
            selectedImageUrl = imagePath; // On met à jour l'URL de l'image sélectionnée

            // 2. Remplir les champs du formulaire
            edtName.setText(name);
            edtDescription.setText(description);

            // 3. Charger l'image existante avec Glide
            if (imagePath != null && !imagePath.isEmpty()) {
                imgRecipePreview.setVisibility(View.VISIBLE);
                Glide.with(this).load(imagePath).into(imgRecipePreview);
            }

            // 4. Mettre à jour l'interface pour le mode édition
            ((TextView) findViewById(R.id.recipeTitle)).setText("Edit Recipe");
            edtName.setEnabled(false); // On bloque le nom pour éviter les erreurs de mise à jour

            // Cacher le bouton "Create" et afficher "Update"
            findButtonByText("Create").setVisibility(View.GONE);
            findButtonByText("Update").setVisibility(View.VISIBLE);

        } else {
            // -- On est en mode CRÉATION --
            // Cacher le bouton "Update"
            findButtonByText("Update").setVisibility(View.GONE);
            findButtonByText("Create").setVisibility(View.VISIBLE);
        }
    }


    private void showImageSearchDialog() {
        if (PEXELS_API_KEY.equals("METTEZ_VOTRE_CLÉ_API_PEXELS_ICI")) {
            toast("Error: Pexels API key not set.");
            return;
        }

        EditText input = new EditText(this);
        input.setHint("Ex: spaghetti, pizza...");

        new AlertDialog.Builder(this)
                .setTitle("Search an image")
                .setView(input)
                .setPositiveButton("Search", (dialog, which) -> {
                    String query = input.getText().toString().trim();
                    if (!query.isEmpty()) {
                        searchImages(query);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void searchImages(String query) {
        toast("Searching...");
        executor.execute(() -> {
            try {
                // Construction de l'URL pour l'API Pexels
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                URL url = new URL("https://api.pexels.com/v1/search?query=" + encodedQuery + "&per_page=15");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", PEXELS_API_KEY);
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == 200) {
                    // Lecture de la réponse JSON
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    PexelsResponse pexelsResponse = gson.fromJson(reader, PexelsResponse.class);
                    reader.close();

                    // Affichage des résultats sur le thread principal
                    handler.post(() -> showImageSelectionDialog(pexelsResponse.photos));
                } else {
                    handler.post(() -> {
                        try {
                            toast("Search error: " + connection.getResponseCode());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } catch (IOException e) {
                handler.post(() -> toast("Search error: " + e.getMessage()));
            }
        });
    }

    private void showImageSelectionDialog(List<PexelsPhoto> photos) {
        if (photos == null || photos.isEmpty()) {
            toast("No images found.");
            return;
        }

        // Création du RecyclerView pour afficher la grille
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 colonnes

        // Création de l'adapter
        ImageAdapter adapter = new ImageAdapter(photos, photo -> {
            selectedImageUrl = photo.src.medium; // On choisit la taille "medium"
            imgRecipePreview.setVisibility(View.VISIBLE);

            // On utilise Glide pour charger l'image depuis l'URL
            Glide.with(CreateRecipeActivity.this)
                    .load(selectedImageUrl)
                    .into(imgRecipePreview);

            // On ferme la boîte de dialogue (astuce pour la retrouver)
            ((AlertDialog) recyclerView.getTag()).dismiss();
        });

        recyclerView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select an image")
                .setView(recyclerView)
                .setNegativeButton("Cancel", null)
                .show();

        // Astuce pour pouvoir fermer la dialog depuis l'adapter
        recyclerView.setTag(dialog);
    }

    // 🛑 SUPPRESSION de la méthode loadRecipeData(String name) qui n'est plus utile
    // private void loadRecipeData(String name) { ... }

    public void onCreateRecipe(View v) {
        String name = edtName.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            toast("Name is required");
            return;
        }

        // ✨ Utilisation de selectedImageUrl au lieu d'un EditText
        long rowId = dao.insertIfAbsent(name, desc, selectedImageUrl);
        if (rowId != -1) {
            toast("Recipe created");
            finish(); // On ferme l'activité après la création
        } else {
            toast("Recipe already exists");
        }
    }

    public void onUpdateRecipe(View v) {
        String name = edtName.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            toast("Name is required");
            return;
        }

        // La vérification 'exists' n'est plus nécessaire car on vient du mode édition

        int rows = dao.updateByName(name, desc, selectedImageUrl);
        if (rows > 0) {
            toast("Recipe updated");
            finish(); // On ferme l'activité après la mise à jour
        } else {
            toast("No change detected");
        }
    }

    public void onDeleteRecipe(View v) {
        String name = edtName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            toast("Name is required");
            return;
        }
        int rows = dao.deleteByName(name);
        if (rows > 0) {
            toast("Recipe deleted");
            finish(); // On ferme l'activité après la suppression
        } else {
            toast("Recipe not found");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dao != null) dao.close();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * ✅ NOUVELLE MÉTHODE UTILITAIRE
     * Trouve un bouton dans la vue en fonction de son texte.
     */
    private Button findButtonByText(String text) {
        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        ArrayList<View> views = new ArrayList<>();
        rootView.findViewsWithText(views, text, View.FIND_VIEWS_WITH_TEXT);
        for (View v : views) {
            if (v instanceof Button) {
                return (Button) v;
            }
        }
        return null; // Retourne null si non trouvé
    }

    // Ces classes correspondent à la structure du JSON renvoyé par l'API Pexels
    private static class PexelsResponse {
        List<PexelsPhoto> photos;
    }

    protected static class PexelsPhoto {
        int id;
        PhotoSource src;
    }

    protected static class PhotoSource {
        String original;
        String large2x;
        String large;
        String medium;
        String small;
        String portrait;
        String landscape;
        String tiny;
    }
}
