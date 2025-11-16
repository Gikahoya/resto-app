package com.utaste.service;

import com.utaste.domain.recipe.Ingredient;
import com.utaste.domain.recipe.Recipe;
import com.utaste.domain.recipe.RecipeIngredient;
import com.utaste.domain.recipe.RecipeRepository;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests unitaires pour :
 *  - la gestion des ingrédients dans une recette (RecipeIngredient + RecipeRepository)
 *  - la logique de base de Ingredient (unités, affichage, etc.)
 *
 * Ces tests sont pensés pour être des tests "logique métier" (pas de SQLite, pas d'Android UI).
 */
public class IngredientRecipeTest {

    private Recipe recipe1;
    private Recipe recipe2;
    private Ingredient tomato;
    private Ingredient cheese;

    @Before
    public void setUp() throws Exception {
        // On réinitialise la liste statique associations de RecipeIngredient
        // pour que chaque test soit indépendant.
        Field f = RecipeIngredient.class.getDeclaredField("associations");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<RecipeIngredient> current = (List<RecipeIngredient>) f.get(null);
        if (current != null) {
            current.clear();
        }

        // On crée quelques recettes et ingrédients pour les tests
        recipe1 = new Recipe("Pâtes bolo", "Une description savoureuse", null);
        recipe2 = new Recipe("Pizza 4 fromages", "Une autre description", null);

        tomato = new Ingredient("Tomate", null, 100.0, Ingredient.Unit.GRAMME);
        cheese = new Ingredient("Fromage", null, 50.0, Ingredient.Unit.GRAMME);
    }

    // 1) Création d'association ingrédient-recette
    @Test
    public void createRecipeIngredient_shouldAppearInGetByRecipe() {
        List<RecipeIngredient> before = RecipeIngredient.getByRecipe(recipe1);
        assertEquals(0, before.size());

        new RecipeIngredient(recipe1, tomato, 100.0);

        List<RecipeIngredient> after = RecipeIngredient.getByRecipe(recipe1);
        assertEquals(1, after.size());
        assertEquals(tomato, after.get(0).getIngredient());
        assertEquals(100.0, after.get(0).getQuantity(), 0.001);
    }

    // 2) getByRecipe() doit retourner uniquement les ingrédients de la recette demandée
    @Test
    public void getByRecipe_shouldReturnOnlyAssociationsForThatRecipe() {
        new RecipeIngredient(recipe1, tomato, 100.0);
        new RecipeIngredient(recipe1, cheese, 50.0);
        new RecipeIngredient(recipe2, cheese, 30.0);

        List<RecipeIngredient> list1 = RecipeIngredient.getByRecipe(recipe1);
        List<RecipeIngredient> list2 = RecipeIngredient.getByRecipe(recipe2);

        assertEquals(2, list1.size());
        assertEquals(1, list2.size());

        // Tous les ingrédients de list1 appartiennent à recipe1
        for (RecipeIngredient ri : list1) {
            assertEquals(recipe1, ri.getRecipe());
        }
        // Et ceux de list2 appartiennent à recipe2
        for (RecipeIngredient ri : list2) {
            assertEquals(recipe2, ri.getRecipe());
        }
    }

    // 3) getByIngredient() doit retourner toutes les recettes utilisant cet ingrédient
    @Test
    public void getByIngredient_shouldReturnAllRecipesUsingIngredient() {
        new RecipeIngredient(recipe1, cheese, 50.0);
        new RecipeIngredient(recipe2, cheese, 30.0);
        new RecipeIngredient(recipe2, tomato, 20.0);

        List<RecipeIngredient> cheeseAssoc = RecipeIngredient.getByIngredient(cheese);
        List<RecipeIngredient> tomatoAssoc = RecipeIngredient.getByIngredient(tomato);

        assertEquals(2, cheeseAssoc.size());
        assertEquals(1, tomatoAssoc.size());

        for (RecipeIngredient ri : cheeseAssoc) {
            assertEquals(cheese, ri.getIngredient());
        }
        for (RecipeIngredient ri : tomatoAssoc) {
            assertEquals(tomato, ri.getIngredient());
        }
    }

    // 4) removeIngredientFromRecipe() doit supprimer seulement l'association ciblée
    @Test
    public void removeIngredientFromRecipe_shouldRemoveOnlyMatchingAssociation() {
        new RecipeIngredient(recipe1, cheese, 50.0);
        new RecipeIngredient(recipe1, tomato, 100.0);
        new RecipeIngredient(recipe2, cheese, 30.0);

        RecipeIngredient.removeIngredientFromRecipe(recipe1, cheese);

        List<RecipeIngredient> list1 = RecipeIngredient.getByRecipe(recipe1);
        List<RecipeIngredient> list2 = RecipeIngredient.getByRecipe(recipe2);

        // Dans recipe1, il ne doit plus y avoir cheese, mais tomate doit rester
        assertEquals(1, list1.size());
        assertEquals(tomato, list1.get(0).getIngredient());

        // Dans recipe2, cheese doit toujours être là
        assertEquals(1, list2.size());
        assertEquals(cheese, list2.get(0).getIngredient());
    }

    // 5) removeRecipe() doit supprimer toutes les associations pour cette recette
    @Test
    public void removeRecipe_shouldRemoveAllAssociationsForThatRecipe() {
        new RecipeIngredient(recipe1, cheese, 50.0);
        new RecipeIngredient(recipe1, tomato, 100.0);
        new RecipeIngredient(recipe2, cheese, 30.0);

        RecipeIngredient.removeRecipe(recipe1);

        List<RecipeIngredient> list1 = RecipeIngredient.getByRecipe(recipe1);
        List<RecipeIngredient> list2 = RecipeIngredient.getByRecipe(recipe2);

        assertEquals(0, list1.size()); // plus aucune association pour recipe1
        assertEquals(1, list2.size()); // recipe2 toujours associée à cheese
    }

    // 6) RecipeRepository.getAllRecipes() doit retourner une copie défensive
    @Test
    public void getAllRecipes_shouldReturnDefensiveCopy() {
        RecipeRepository repo = new RecipeRepository();
        repo.addRecipe(recipe1);
        repo.addRecipe(recipe2);

        List<Recipe> list = repo.getAllRecipes();
        assertEquals(2, list.size());

        // On modifie la liste retournée, mais pas le repo interne
        list.clear();
        List<Recipe> list2 = repo.getAllRecipes();
        assertEquals(2, list2.size());
    }

    // 7) RecipeRepository.addRecipe() doit refuser les doublons (même nom)
    @Test(expected = IllegalArgumentException.class)
    public void addRecipe_withDuplicateName_shouldThrowException() {
        RecipeRepository repo = new RecipeRepository();
        repo.addRecipe(recipe1);

        // Même nom (ignore case) → doit déclencher IllegalArgumentException
        Recipe sameName = new Recipe("pâtes bolo", "desc", null);
        repo.addRecipe(sameName);
    }

    // 8) findRecipeByName() doit ignorer la casse
    @Test
    public void findRecipeByName_shouldIgnoreCase() {
        RecipeRepository repo = new RecipeRepository();
        repo.addRecipe(recipe1);

        assertTrue(repo.findRecipeByName("pâtes bolo").isPresent());
        assertTrue(repo.findRecipeByName("PÂTES BOLO").isPresent());
        assertFalse(repo.findRecipeByName("pizza").isPresent());
    }

    // 9) Ingredient.Unit.fromString() doit correctement interpréter plusieurs unités
    @Test
    public void ingredientUnit_fromString_shouldParseCommonUnits() {
        assertEquals(Ingredient.Unit.GRAMME, Ingredient.Unit.fromString("g"));
        assertEquals(Ingredient.Unit.GRAMME, Ingredient.Unit.fromString("gramme"));
        assertEquals(Ingredient.Unit.LITRE, Ingredient.Unit.fromString("L"));
        assertEquals(Ingredient.Unit.MILLILITRE, Ingredient.Unit.fromString("ml"));
        assertEquals(Ingredient.Unit.PAQUET, Ingredient.Unit.fromString("paquets"));
        assertEquals(Ingredient.Unit.PIECE, Ingredient.Unit.fromString("truc inconnu"));
    }

    // 10) Ingredient.getDisplayQuantity() doit bien formater la quantité + unité
    @Test
    public void ingredient_getDisplayQuantity_shouldFormatAmountAndUnit() {
        Ingredient ing = new Ingredient("Farine", null, 250.0, Ingredient.Unit.GRAMME);
        String display = ing.getDisplayQuantity();

        // Exemple attendu : "250 g"
        assertTrue(display.contains("250"));
        assertTrue(display.toLowerCase().contains("g"));
    }
}
