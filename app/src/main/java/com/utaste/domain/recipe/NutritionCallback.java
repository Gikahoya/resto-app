package com.utaste.domain.recipe;

public interface NutritionCallback { 
    void onSuccess(NutritionFact fact);
    void onError(String msg);
}
