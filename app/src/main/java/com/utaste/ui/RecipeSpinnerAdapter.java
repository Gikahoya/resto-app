package com.utaste.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.utaste.R;
import com.utaste.domain.recipe.Recipe;

import java.util.List;

public class RecipeSpinnerAdapter extends ArrayAdapter<Recipe> {

    public RecipeSpinnerAdapter(@NonNull Context context, @NonNull List<Recipe> recipeList) {
        super(context, 0, recipeList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent, false);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent, true);
    }

    private View createItemView(int position, View convertView, ViewGroup parent, boolean isDropDown) {
        int layoutId = isDropDown ? R.layout.spinner_item_recipe_dropdown : R.layout.spinner_item_recipe;
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
        }

        Recipe currentRecipe = getItem(position);

        if (currentRecipe != null) {
            TextView recipeName = view.findViewById(R.id.tvRecipeName);
            recipeName.setText(currentRecipe.getName());

            if (isDropDown) {
                ImageView recipeImage = view.findViewById(R.id.ivRecipeImage);
                Glide.with(getContext())
                        .load(currentRecipe.getImagePath())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(recipeImage);
            }
        }

        return view;
    }
}