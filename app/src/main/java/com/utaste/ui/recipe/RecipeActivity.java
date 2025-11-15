package com.utaste.ui.recipe;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecipeActivity extends AppCompatActivity {

    private static final String PEXELS_API_KEY = "SSXpX9eI3YazHuoWxgA5mFEHSguIl04baBvbLOyNGo7vcCidyUND9uLX";   // API key for Pexels

    // Composants de l'interface
    private EditText edtName, edtDescription;
    private ImageView imgRecipePreview; // Remplacement de l'ancien EditText d'image
    private Button btnSearchImage;

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
        setContentView(R.layout.activity_recipe);

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

        edtName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String name = edtName.getText().toString().trim();
                if(!name.isEmpty()) {
                    loadRecipeData(name);
                }
            }
        });
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
            Glide.with(RecipeActivity.this)
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

    private void loadRecipeData(String name) {
        executor.execute(() -> {
            Recipe recipe = dao.findByName(name);

            handler.post(() -> {
                if(recipe != null) {
                    edtDescription.setText(recipe.getDescription());
                    selectedImageUrl = recipe.getImagePath();

                    if (selectedImageUrl != null && !selectedImageUrl.isEmpty()) {
                        imgRecipePreview.setVisibility(View.VISIBLE);
                        Glide.with(RecipeActivity.this)
                                .load(selectedImageUrl)
                                .into(imgRecipePreview);
                    } else {
                        imgRecipePreview.setVisibility(View.GONE);
                    }
                } else {
                    edtDescription.setText("");
                    imgRecipePreview.setVisibility(View.GONE);
                    selectedImageUrl = null;
                }
            });
        });
    }

    public void onCreateRecipe(View v) {
        String name = edtName.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            toast("Name is required");
            return;
        }

        // ✨ Utilisation de selectedImageUrl au lieu d'un EditText
        long rowId = dao.insertIfAbsent(name, desc, selectedImageUrl);
        toast(rowId == -1 ? "Recipe already exists" : "Recipe created");
    }

    public void onUpdateRecipe(View v) {
        String name = edtName.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            toast("Name is required");
            return;
        }
        if (!dao.exists(name)) {
            toast("Recipe not found");
            return;
        }

        // ✨ Utilisation de selectedImageUrl au lieu d'un EditText
        int rows = dao.updateByName(name, desc, selectedImageUrl);
        toast(rows > 0 ? "Recipe updated" : "No change");
    }

    public void onDeleteRecipe(View v) {
        String name = edtName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            toast("Name is required");
            return;
        }
        int rows = dao.deleteByName(name);
        if (rows > 0) {
            // ✨ On réinitialise l'image après suppression
            imgRecipePreview.setVisibility(View.GONE);
            imgRecipePreview.setImageDrawable(null);
            selectedImageUrl = null;
        }
        toast(rows > 0 ? "Recipe deleted" : "Recipe not found");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dao != null) dao.close();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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