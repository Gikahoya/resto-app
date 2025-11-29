package com.utaste.ui.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.utaste.R;
import com.utaste.data.sqlite.RecipeDao;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// ✅ CORRECTION : Les classes de modèle ne sont plus des classes internes statiques
public class CreateRecipeActivity extends AppCompatActivity {

    private static final String PEXELS_API_KEY = "SSXpX9eI3YazHuoWxgA5mFEHSguIl04baBvbLOyNGo7vcCidyUND9uLX";

    // Vues
    private EditText edtName, edtDescription;
    private ImageView imgRecipePreview;
    private Button btnSearchImage, btnCreate, btnUpdate, btnDelete, btnManageIngredients;
    private TextView recipeTitle;

    // Données
    private RecipeDao dao;
    private String selectedImageUrl = null;
    private long currentRecipeId = -1L;
    private String originalRecipeName = null; // Important pour updateByName/deleteByName

    // Outils
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        initViews();
        setupListeners();
        dao = new RecipeDao(this);
        checkForEditMode();
    }

    private void initViews() {
        recipeTitle = findViewById(R.id.recipeTitle);
        edtName = findViewById(R.id.edtName);
        edtDescription = findViewById(R.id.edtDescription);
        imgRecipePreview = findViewById(R.id.imgRecipePreview);
        btnSearchImage = findViewById(R.id.btnSearchImage);

        // Liaison des boutons d'action
        btnCreate = findViewById(R.id.btnCreate);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSearchImage.setOnClickListener(v -> showImageSearchDialog());

        // Configuration des listeners pour les boutons d'action
        btnCreate.setOnClickListener(this::onCreateRecipe);
        btnUpdate.setOnClickListener(this::onUpdateRecipe);
        btnDelete.setOnClickListener(this::onDeleteRecipe);
    }

    private void checkForEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("RECIPE_ID")) {
            // ----- MODE ÉDITION -----
            recipeTitle.setText("Edit Recipe");

            // Récupérer les données passées par RecipeDetailsActivity
            currentRecipeId = intent.getLongExtra("RECIPE_ID", -1L);
            originalRecipeName = intent.getStringExtra("RECIPE_NAME"); // Très important
            String description = intent.getStringExtra("RECIPE_DESCRIPTION");
            selectedImageUrl = intent.getStringExtra("RECIPE_IMAGE_PATH");

            // Pré-remplir le formulaire
            edtName.setText(originalRecipeName);
            edtDescription.setText(description);

            // On ne peut pas modifier le nom car il sert de clé pour le DAO
            edtName.setEnabled(false);

            if (selectedImageUrl != null && !selectedImageUrl.isEmpty()) {
                imgRecipePreview.setVisibility(View.VISIBLE);
                Glide.with(this).load(selectedImageUrl).into(imgRecipePreview);
            }

            // Afficher les bons boutons
            btnCreate.setVisibility(View.GONE);
            btnUpdate.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            // ----- MODE CRÉATION -----
            recipeTitle.setText("Create Recipe");
            btnCreate.setVisibility(View.VISIBLE);
            btnUpdate.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }
    }

    // La logique de recherche d'image reste la même
    private void showImageSearchDialog() {
        if (PEXELS_API_KEY.contains("METTEZ_VOTRE_CLÉ")) {
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

// ... (le début du fichier ne change pas)

// ... (le début du fichier CreateRecipeActivity.java ne change pas)

    private void searchImages(String query) {
        toast("Searching...");
        executor.execute(() -> {
            HttpURLConnection connection = null; // Déclarer la connexion ici
            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                URL url = new URL("https://api.pexels.com/v1/search?query=" + encodedQuery + "&per_page=15");

                // ✅ CORRECTION : Utilisation de la déclaration explicite et standard.
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", PEXELS_API_KEY);
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == 200) {
                    // ✅ CORRECTION : Utilisation de la déclaration explicite pour le reader.
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    PexelsResponse pexelsResponse = gson.fromJson(reader, PexelsResponse.class);
                    reader.close();
                    handler.post(() -> showImageSelectionDialog(pexelsResponse.photos));
                } else {
                    // Cette ligne est correcte et nécessaire pour obtenir le code d'erreur HTTP.
                    final int responseCode = connection.getResponseCode();
                    handler.post(() -> toast("Search error: " + responseCode));
                }
            } catch (IOException e) {
                handler.post(() -> toast("Search error: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect(); // Bonne pratique : toujours fermer la connexion.
                }
            }
        });
    }

// ... (la fin du fichier ne change pas)


// ... (la fin du fichier ne change pas)


    private void showImageSelectionDialog(List<PexelsPhoto> photos) {
        if (photos == null || photos.isEmpty()) {
            toast("No images found.");
            return;
        }
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // Le clic sur l'image de l'adapter
        ImageAdapter adapter = new ImageAdapter(photos, photo -> {
            // ✅ Cette ligne est maintenant correcte car PexelsPhoto est visible
            selectedImageUrl = photo.src.medium;
            imgRecipePreview.setVisibility(View.VISIBLE);
            Glide.with(CreateRecipeActivity.this).load(selectedImageUrl).into(imgRecipePreview);
            ((AlertDialog) recyclerView.getTag()).dismiss();
        });

        recyclerView.setAdapter(adapter);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select an image")
                .setView(recyclerView)
                .setNegativeButton("Cancel", null)
                .show();
        recyclerView.setTag(dialog);
    }

    // --- Méthodes d'action des boutons ---

    public void onCreateRecipe(View v) {
        String name = edtName.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            toast("Name is required");
            return;
        }
        long rowId = dao.insertIfAbsent(name, desc, selectedImageUrl);
        if (rowId != -1) {
            toast("Recipe created");
            finish();
        } else {
            toast("Recipe with this name already exists");
        }
    }

    public void onUpdateRecipe(View v) {
        // Le nom n'est pas modifiable, on utilise `originalRecipeName` qui est le nom au chargement
        String desc = edtDescription.getText().toString().trim();
        if (originalRecipeName == null) {
            toast("Error: Original recipe name not found.");
            return;
        }
        int rows = dao.updateByName(originalRecipeName, desc, selectedImageUrl);
        if (rows > 0) {
            toast("Recipe updated");
            finish();
        } else {
            toast("No change detected");
        }
    }

    public void onDeleteRecipe(View v) {
        if (originalRecipeName == null) {
            toast("Error: Original recipe name not found.");
            return;
        }
        int rows = dao.deleteByName(originalRecipeName);
        if (rows > 0) {
            toast("Recipe deleted");
            finish();
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
}

// ✅ CORRECTION : Les classes sont maintenant en dehors de CreateRecipeActivity.
// Elles ont une visibilité "package-private", ce qui les rend accessibles
// à ImageAdapter.java qui est dans le même package (com.utaste.ui.recipe).
class PexelsResponse {
    List<PexelsPhoto> photos;
}

class PexelsPhoto {
    int id;
    PhotoSource src;
}

class PhotoSource {
    String original;
    String large2x;
    String large;
    String medium;
    String small;
    String portrait;
    String landscape;
    String tiny;
}
