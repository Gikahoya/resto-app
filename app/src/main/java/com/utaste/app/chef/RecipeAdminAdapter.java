package com.utaste.app.chef;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.utaste.R;
import com.utaste.data.sqlite.RecipeDao;
import com.utaste.domain.recipe.Recipe;

import java.util.List;
import java.util.stream.Collectors;

public class RecipeAdminAdapter extends RecyclerView.Adapter<RecipeAdminAdapter.RecipeViewHolder> {

    private final Context context;
    private List<Recipe> recipeList;
    private final RecipeDao recipeDao; // ✅ AJOUT: DAO pour les ingrédients

    public RecipeAdminAdapter(Context context, List<Recipe> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
        this.recipeDao = new RecipeDao(context); // ✅ AJOUT: Initialisation
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe_admin, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        holder.recipeName.setText(recipe.getName());

        // ✅ MODIFICATION : Récupérer et afficher les ingrédients
        new Thread(() -> {
            List<RecipeDao.RecipeIngredientRow> ingredients = recipeDao.getIngredientsForRecipe(recipe.getId());
            String ingredientsText = "No ingredients";
            if (!ingredients.isEmpty()) {
                ingredientsText = ingredients.stream()
                        .map(row -> row.ingredient.getName())
                        .collect(Collectors.joining(", "));
            }
            // Mettre à jour l'UI sur le thread principal
            String finalText = ingredientsText;
            holder.itemView.post(() -> holder.recipeIngredients.setText(finalText));
        }).start();

        // Utiliser Glide pour charger l'image
        Glide.with(context)
                .load(recipe.getImagePath())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.recipeImage);

        // Listener de clic pour aller aux détails
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailsActivity.class);
            intent.putExtra("RECIPE_ID", recipe.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public void filterList(List<Recipe> filteredList) {
        this.recipeList = filteredList;
        notifyDataSetChanged();
    }

    // ✅ MODIFICATION : ViewHolder mis à jour pour correspondre au layout
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        TextView recipeName, recipeIngredients;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.ivRecipeImage);
            recipeName = itemView.findViewById(R.id.tvRecipeName);
            recipeIngredients = itemView.findViewById(R.id.tvRecipeIngredients); // Lier le TextView des ingrédients
        }
    }
}
