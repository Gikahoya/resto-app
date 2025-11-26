package com.utaste.domain.sale;

public class Sale {

    private long id;
    private long recipeId;
    private int rating;
    private String appreciation;
    private long timestamp;

    public Sale() {}

    public long getId() { return id; }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecipeId() { return recipeId; }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public int getRating() { return rating; }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getAppreciation() { return appreciation; }

    public void setAppreciation(String appreciation) {
        this.appreciation = appreciation;
    }

    public long getTimestamp() { return timestamp; }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}