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
import com.utaste.domain.recipe.RecipeNutritionSummary;
import com.utaste.data.sqlite.RecipeDao; // Import RecipeDao
import com.utaste.domain.recipe.Recipe;
import com.utaste.domain.recipe.RecipeNutritionService; // Import NutritionService

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeAdminAdapter extends RecyclerView.Adapter<RecipeAdminAdapter.RecipeViewHolder> {

    private final Context context;
    private List<Recipe> recipeList;
    private final RecipeNutritionService nutritionService; // Service to calculate calories

    public RecipeAdminAdapter(Context context, List<Recipe> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
        // Initialize the service with the context to access the database
        this.nutritionService = new RecipeNutritionService(context);
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


        // CORRECTED: Call the existing `computeForRecipeId` method
        RecipeNutritionSummary summary = nutritionService.computeForRecipeId((int) recipe.getId());// CORRECTED: Get the calorie value from the summary object
        double totalCalories = 0.0;
        if (summary != null) {
            totalCalories = summary.getTotalCalories();
        }
        // Format the text and display it
        String calorieText = String.format(Locale.getDefault(), "%.0f kcal", totalCalories);
        holder.recipeCalories.setText(calorieText);


        // Use Glide to load the image
        Glide.with(context)
                .load(recipe.getImagePath())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.recipeImage);

        // Set an OnClickListener for the entire item view
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

    // This method updates the list for the search filter
    public void filterList(List<Recipe> filteredList) {
        this.recipeList = filteredList;
        notifyDataSetChanged();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        // MODIFICATION: The second TextView is now for calories
        TextView recipeName, recipeCalories;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.ivRecipeImage);
            recipeName = itemView.findViewById(R.id.tvRecipeName);
            // MODIFICATION: Find the view by its new ID
            recipeCalories = itemView.findViewById(R.id.tvRecipeCalories);
        }
    }
}
