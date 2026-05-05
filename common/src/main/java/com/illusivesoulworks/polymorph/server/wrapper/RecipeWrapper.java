package com.illusivesoulworks.polymorph.server.wrapper;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

public class RecipeWrapper {

  private final RecipeHolder<?> recipe;
  private final List<IngredientWrapper> ingredients;

  public RecipeWrapper(RecipeHolder<?> pRecipe) {
    this.recipe = pRecipe;
    this.ingredients = new ArrayList<>();

    // Use placementInfo to get ingredients if available
    var placementInfo = this.recipe.value().placementInfo();
    if (placementInfo != null) {
      for (Ingredient ingredient : placementInfo.ingredients()) {
        IngredientWrapper wrapped = new IngredientWrapper(ingredient);
        this.ingredients.add(wrapped);
      }
    }
  }

  public Recipe<?> getRecipe() {
    return this.recipe.value();
  }

  public Identifier getId() {
    return this.recipe.id().identifier();
  }

  public List<IngredientWrapper> getIngredients() {
    return this.ingredients;
  }

  public boolean conflicts(RecipeWrapper pOther) {

    if (pOther == null) {
      return false;
    } else if (this.getId().equals(pOther.getId())) {
      return true;
    } else if (this.ingredients.size() != pOther.getIngredients().size()) {
      return false;
    } else {
      List<IngredientWrapper> otherIngredients = pOther.getIngredients();

      for (int i = 0; i < otherIngredients.size(); i++) {

        if (!otherIngredients.get(i).matches(this.getIngredients().get(i))) {
          return false;
        }
      }
      return true;
    }
  }
}
