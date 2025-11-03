package com.utaste.ui.recipe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.utaste.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    public interface OnImageClickListener {
        void onImageClick(RecipeActivity.PexelsPhoto photo);
    }

    private final List<RecipeActivity.PexelsPhoto> photos;
    private final OnImageClickListener listener;

    public ImageAdapter(List<RecipeActivity.PexelsPhoto> photos, OnImageClickListener listener) {
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

    @NonNull
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        RecipeActivity.PexelsPhoto photo = photos.get(position);
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

        void bind(final RecipeActivity.PexelsPhoto photo, final OnImageClickListener listener) {
            Glide.with(itemView.getContext())
                    .load(photo.src.tiny)
                    .into(imageView);
            itemView.setOnClickListener(v -> listener.onImageClick(photo));
        }
    }

}
