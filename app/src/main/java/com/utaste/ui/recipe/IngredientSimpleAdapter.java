package com.utaste.ui.recipe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;import androidx.recyclerview.widget.RecyclerView;

import com.utaste.domain.recipe.Ingredient;
import java.util.ArrayList;
import java.util.List;

public class IngredientSimpleAdapter extends RecyclerView.Adapter<IngredientSimpleAdapter.ViewHolder> {

    public interface OnIngredientClickListener {
        void onIngredientClick(Ingredient ingredient);
    }

    private final List<Ingredient> ingredients = new ArrayList<>();
    private final OnIngredientClickListener listener;

    public IngredientSimpleAdapter(OnIngredientClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Ingredient> newIngredients) {
        ingredients.clear();
        ingredients.addAll(newIngredients);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.bind(ingredient, listener);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }

        void bind(final Ingredient ingredient, final OnIngredientClickListener listener) {
            textView.setText(ingredient.getName());
            itemView.setOnClickListener(v -> listener.onIngredientClick(ingredient));
        }
    }
}