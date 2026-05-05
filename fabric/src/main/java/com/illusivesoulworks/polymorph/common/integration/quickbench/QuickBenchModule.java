//package com.illusivesoulworks.polymorph.common.integration.quickbench;
//
//import com.illusivesoulworks.polymorph.common.integration.AbstractCompatibilityModule;
//import com.illusivesoulworks.polymorph.mixin.core.AccessorCraftingMenu;
//import net.minecraft.world.inventory.AbstractContainerMenu;
//import net.minecraft.world.inventory.AbstractCraftingMenu;
//import net.minecraft.world.inventory.ResultContainer;
//import net.minecraft.world.item.crafting.CraftingRecipe;
//import net.minecraft.world.item.crafting.RecipeHolder;
//
//public class QuickBenchModule extends AbstractCompatibilityModule {
//
//  @Override
//  public boolean selectRecipe(AbstractContainerMenu containerMenu, RecipeHolder<?> recipe) {
//
//    if (recipe.value() instanceof CraftingRecipe) {
//      ResultContainer result = null;
//
//      if (containerMenu instanceof AbstractCraftingMenu) {
//        AccessorCraftingMenu accessor = (AccessorCraftingMenu) containerMenu;
//        result = accessor.getResultSlots();
//      }
//
//      if (result != null) {
//        result.setRecipeUsed(recipe);
//      }
//    }
//    return false;
//  }
//}
