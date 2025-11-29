package com.utaste.ui.recipe;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

// ✅ CORRIGÉ : L'adaptateur n'a plus de dépendance vers CreateRecipeActivity
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    // Interface pour gérer le clic sur une image
    public interface OnImageClickListener {
        void onImageClick(PexelsPhoto photo); // ✅ Utilise la classe de modèle indépendante
    }

    private final List<PexelsPhoto> photos;
    private final OnImageClickListener listener;

    public ImageAdapter(List<PexelsPhoto> photos, OnImageClickListener listener) {
        this.photos = photos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(300, 300));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(4, 4, 4, 4);
        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        PexelsPhoto photo = photos.get(position); // ✅ Utilise la classe de modèle indépendante
        holder.bind(photo, listener);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }

        void bind(final PexelsPhoto photo, final OnImageClickListener listener) { // ✅ Utilise la classe de modèle indépendante
            // On charge la petite image pour la grille pour de meilleures performances
            Glide.with(itemView.getContext())
                    .load(photo.src.tiny)
                    .into(imageView);

            // Configure le listener de clic
            itemView.setOnClickListener(v -> listener.onImageClick(photo));
        }
    }
}
