package com.illusivesoulworks.polymorph.common.integration.util;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.illusivesoulworks.polymorph.api.client.base.IRecipesWidget;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeTransfer {

  private static Identifier transfer = null;

  public static void enqueueTransfer(Identifier resourceLocation) {
    transfer = resourceLocation;
  }

  public static Identifier getTransfer() {
    return transfer;
  }

  public static void selectRecipe(RecipeHolder<?> recipe) {
    selectRecipe(recipe.id().identifier());
  }

  public static void selectRecipe(Identifier resourceLocation) {
    IRecipesWidget widget = PolymorphWidgets.getInstance().getCurrentWidget();

    if (widget != null) {
      widget.selectRecipe(resourceLocation);
    }
  }
}
