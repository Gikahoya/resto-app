package com.utaste.domain.recipe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.utaste.R;

import java.util.List;

public class IngredientAdapter extends ArrayAdapter<Ingredient> {

    // ===== INTERFACE MISE À JOUR =====
    public interface Listener {
        void onEdit(Ingredient ingredient);
        void onDelete(Ingredient ingredient);
        void onShowInfo(Ingredient ingredient); // NOUVELLE MÉTHODE
    }
    // ==================================

    private final LayoutInflater inflater;
    private final Listener listener;

    public IngredientAdapter(Context context, List<Ingredient> ingredients, Listener listener) {
        super(context, 0, ingredients);
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_ingredient, parent, false);
        }

        Ingredient ingredient = getItem(position);

        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtQuantity = view.findViewById(R.id.txtQuantity);
        TextView txtQr = view.findViewById(R.id.txtQrCode);
        Button btnEdit = view.findViewById(R.id.btnEdit);
        Button btnDelete = view.findViewById(R.id.btnDelete);

        if (ingredient != null) {
            txtName.setText(ingredient.getName());
            txtQuantity.setText(ingredient.getDisplayQuantity());
            txtQr.setText(ingredient.getQrCode() == null || ingredient.getQrCode().isEmpty() ? "No QR Code" : "QR: " + ingredient.getQrCode());

            // Listeners pour les boutons
            btnEdit.setOnClickListener(v -> listener.onEdit(ingredient));
            btnDelete.setOnClickListener(v -> listener.onDelete(ingredient));

            // ===== NOUVEAUX LISTENERS =====
            // On rend le texte cliquable pour afficher les infos
            txtName.setOnClickListener(v -> listener.onShowInfo(ingredient));
            txtQuantity.setOnClickListener(v -> listener.onShowInfo(ingredient));
            txtQr.setOnClickListener(v -> listener.onShowInfo(ingredient));
            // ================================
        }

        return view;
    }
}