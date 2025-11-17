package com.utaste.ui.recipe;

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
import com.utaste.domain.recipe.RecipeIngredient;
import java.util.List;
import java.util.Locale;

public class RecipeIngredientAdapter extends ArrayAdapter<RecipeIngredient> {

    public interface Listener {
        void onEdit(RecipeIngredient recipeIngredient);
        void onDelete(RecipeIngredient recipeIngredient);
    }

    private final LayoutInflater inflater;
    private final Listener listener;

    public RecipeIngredientAdapter(Context context, List<RecipeIngredient> ingredients, Listener listener) {
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

        RecipeIngredient recipeIngredient = getItem(position);

        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtQuantity = view.findViewById(R.id.txtQuantity);
        TextView txtQr = view.findViewById(R.id.txtQrCode);
        Button btnEdit = view.findViewById(R.id.btnEdit);
        Button btnDelete = view.findViewById(R.id.btnDelete);

        if (recipeIngredient != null && recipeIngredient.getIngredient() != null) {
            txtName.setText(recipeIngredient.getIngredient().getName());
            txtQuantity.setText(String.format(Locale.US, "Quantity: %.1f%%", recipeIngredient.getQuantity()));
            txtQr.setText("QR: " + recipeIngredient.getIngredient().getQrCode());

            btnEdit.setOnClickListener(v -> listener.onEdit(recipeIngredient));
            btnDelete.setOnClickListener(v -> listener.onDelete(recipeIngredient));
        }

        return view;
    }
}