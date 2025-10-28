package com.utaste.domain.recipe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.utaste.R;

import java.util.ArrayList;
import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientVH> {

    public interface Listener {
        void onEdit(Ingredient ingredient);
        void onDelete(Ingredient ingredient);
    }

    private final List<Ingredient> data = new ArrayList<>();
    private final Listener listener;

    public IngredientAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Ingredient> newData) {
        data.clear();
        if (newData != null) {
            data.addAll(newData);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IngredientVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new IngredientVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientVH holder, int position) {
        Ingredient ingredient = data.get(position);
        holder.bind(ingredient, listener);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class IngredientVH extends RecyclerView.ViewHolder {
        final TextView txtName, txtQuantity, txtQr;
        final Button btnEdit, btnDelete; // Corrigé: Button au lieu de ImageButton

        IngredientVH(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtQr = itemView.findViewById(R.id.txtQrCode);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(final Ingredient ingredient, final Listener listener) {
            txtName.setText(ingredient.getName());
            txtQuantity.setText(ingredient.getDisplayQuantity());
            txtQr.setText(ingredient.getQrCode() == null || ingredient.getQrCode().isEmpty() ? "Pas de code QR" : "QR: " + ingredient.getQrCode());

            btnEdit.setOnClickListener(v -> listener.onEdit(ingredient));
            btnDelete.setOnClickListener(v -> listener.onDelete(ingredient));
        }
    }
}
