package com.utaste.domain.sale;
public class RecipeSalesSummary {

    private final long recipeId;
    private final String recipeName;
    private final int salesCount;
    private final double averageRating;

    public RecipeSalesSummary(long recipeId, String recipeName, int salesCount, double averageRating) {
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.salesCount = salesCount;
        this.averageRating = averageRating;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public int getSalesCount() {
        return salesCount;
    }

    public double getAverageRating() {
        return averageRating;
    }
}
