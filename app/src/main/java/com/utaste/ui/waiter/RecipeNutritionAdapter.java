package com.utaste.ui.waiter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.utaste.R;
import com.utaste.domain.recipe.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeNutritionAdapter extends RecyclerView.Adapter<RecipeNutritionAdapter.ViewHolder> {

    public static class RecipeInfo {
        public final Recipe recipe;
        public final double totalCalories;

        public RecipeInfo(Recipe recipe, double totalCalories) {
            this.recipe = recipe;
            this.totalCalories = totalCalories;
        }
    }

    private final List<RecipeInfo> recipeInfoList = new ArrayList<>();

    public RecipeNutritionAdapter() {
    }

    public void submitList(List<RecipeInfo> newRecipeInfoList) {
        recipeInfoList.clear();
        recipeInfoList.addAll(newRecipeInfoList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_nutrition, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeInfo recipeInfo = recipeInfoList.get(position);
        holder.bind(recipeInfo);
    }

    @Override
    public int getItemCount() {
        return recipeInfoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView recipeImage;
        private final TextView recipeName;
        private final TextView recipeCalories;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipe_image);
            recipeName = itemView.findViewById(R.id.recipe_name);
            recipeCalories = itemView.findViewById(R.id.recipe_calories);
        }

        public void bind(RecipeInfo recipeInfo) {
            recipeName.setText(recipeInfo.recipe.getName());

            String calorieText = String.format(Locale.getDefault(), "~ %.0f kcal", recipeInfo.totalCalories);
            recipeCalories.setText(calorieText);

            Glide.with(itemView.getContext())
                    .load(recipeInfo.recipe.getImagePath())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(recipeImage);
        }
    }
}