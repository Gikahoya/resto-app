package com.utaste.app.chef;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.utaste.R;
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.domain.recipe.Recipe;
import com.utaste.ui.recipe.CreateRecipeActivity;


public class RecipeDetailsActivity extends AppCompatActivity {

    // --- UI Views ---
    private ImageView recipeImageDetail;
    private TextView recipeTitleDetail;
    private TextView recipeDescriptionDetail;
    private ImageButton btnBackDetail;
    private FloatingActionButton fabEditDescription;
    private FloatingActionButton fabEditImage;

    // --- Data ---
    private RecipeDao recipeDao;
    private long currentRecipeId = -1;
    private Recipe currentRecipe; // NOUVEAU : Objet pour stocker la recette chargée

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        // Initialiser l'accès à la base de données
        recipeDao = new RecipeDao(this);

        // Lier les variables Java aux vues du layout XML
        initViews();

        // Récupérer l'ID de la recette passé depuis l'activité précédente
        currentRecipeId = getIntent().getLongExtra("RECIPE_ID", -1);

        // Configurer les écouteurs de clics pour les boutons
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Charger ou recharger les données de la recette lorsque l'activité devient visible
        // C'est utile si l'utilisateur revient de l'écran d'édition
        if (currentRecipeId != -1) {
            loadRecipeData();
        } else {
            // Si aucun ID n'a été passé, afficher une erreur et fermer l'activité
            Toast.makeText(this, "Error: Recipe not found.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Initialise toutes les vues à partir du fichier de layout.
     */
    private void initViews() {
        recipeImageDetail = findViewById(R.id.iv_recipe_image_detail);
        recipeTitleDetail = findViewById(R.id.tv_recipe_title_detail);
        recipeDescriptionDetail = findViewById(R.id.tv_recipe_description_detail);
        btnBackDetail = findViewById(R.id.btn_back_detail);
        fabEditDescription = findViewById(R.id.fab_edit_description);
        fabEditImage = findViewById(R.id.fab_edit_image);
    }

    /**
     * Récupère les données de la recette depuis la base de données et peuple l'interface utilisateur.
     */
    private void loadRecipeData() {
        // CORRECTION : Stocker la recette trouvée dans la variable de classe
        currentRecipe = recipeDao.findById(currentRecipeId);

        if (currentRecipe != null) {
            // Remplir les vues avec les données de la recette
            recipeTitleDetail.setText(currentRecipe.getName());
            recipeDescriptionDetail.setText(currentRecipe.getDescription());

            // Utiliser Glide pour charger l'image depuis son chemin dans l'ImageView
            Glide.with(this)
                    .load(currentRecipe.getImagePath())
                    .placeholder(R.drawable.ic_launcher_background) // Image affichée pendant le chargement
                    .error(R.drawable.ic_launcher_background)       // Image affichée en cas d'erreur
                    .into(recipeImageDetail);
        } else {
            // Gérer le cas où la recette n'a pas pu être trouvée
            Toast.makeText(this, "Could not load recipe details.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Configure les OnClickListeners pour tous les boutons interactifs.
     */
    private void setupListeners() {
        // Le bouton Retour termine simplement l'activité en cours
        btnBackDetail.setOnClickListener(v -> finish());

        // CORRECTION : Listener pour le bouton d'édition
        fabEditDescription.setOnClickListener(v -> {
            if (currentRecipe != null) {
                // Créer une Intent pour lancer CreateRecipeActivity
                Intent intent = new Intent(RecipeDetailsActivity.this, CreateRecipeActivity.class);

                // Mettre les informations de la recette actuelle dans l'Intent
                intent.putExtra("RECIPE_ID", currentRecipe.getId());
                intent.putExtra("RECIPE_NAME", currentRecipe.getName());
                intent.putExtra("RECIPE_DESCRIPTION", currentRecipe.getDescription());
                intent.putExtra("RECIPE_IMAGE_PATH", currentRecipe.getImagePath());

                // Démarrer l'activité
                startActivity(intent);
            } else {
                Toast.makeText(this, "Cannot edit a recipe that is not loaded.", Toast.LENGTH_SHORT).show();
            }
        });

        // Le listener pour fabEditImage peut être le même ou différent si besoin
        fabEditImage.setOnClickListener(v -> {
            // Pour l'instant, il fait la même chose, mais vous pourriez changer cela plus tard
            // pour ouvrir, par exemple, directement la galerie d'images.
            fabEditDescription.performClick();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Fermer la connexion à la base de données pour éviter les fuites de mémoire
        if (recipeDao != null) {
            recipeDao.close();
        }
    }
}
