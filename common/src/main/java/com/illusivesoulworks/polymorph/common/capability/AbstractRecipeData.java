/*
 * Copyright (C) 2020-2022 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Polymorph is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Polymorph.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.illusivesoulworks.polymorph.common.capability;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.api.common.capability.IRecipeData;
import com.illusivesoulworks.polymorph.common.util.RecipePair;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.*;

public abstract class AbstractRecipeData<E> implements IRecipeData<E> {

  private final SortedSet<IRecipePair> recipesList;
  private final E owner;
  private final RecipeCache recipeCache;
  private final Map<UUID, ServerPlayer> listeners;

  private RecipeHolder<?> selectedRecipe;
  protected Identifier loadedRecipe;

  public AbstractRecipeData(E owner) {
    this.recipesList = new TreeSet<>();
    this.owner = owner;
    this.recipeCache = new RecipeCache(10);
    this.listeners = new HashMap<>();
  }

  @Override
  public <I extends RecipeInput, T extends Recipe<I>> RecipeHolder<T> getRecipe(
      RecipeType<T> type, I recipeInput, Level level, List<RecipeHolder<T>> recipesListIn) {
    List<RecipeHolder<T>> recipes =
        recipesListIn.isEmpty() ? this.recipeCache.get(level, type, recipeInput) : recipesListIn;

    if (recipes.isEmpty()) {
      this.updateRecipesList(new TreeSet<>());
      return null;
    }
    RegistryAccess registryAccess = level.registryAccess();

    if (this.loadedRecipe != null && this.getSelectedRecipe() == null) {
      if (level instanceof ServerLevel serverLevel) {
        ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, this.loadedRecipe);
        serverLevel.recipeAccess().byKey(recipeKey).ifPresent(this::setSelectedRecipe);
      }
    }
    this.loadedRecipe = null;
    RecipeHolder<T> firstResult = null;
    RecipeHolder<T> selected = null;
    SortedSet<IRecipePair> recipesList = new TreeSet<>();

    for (RecipeHolder<T> entry : recipes) {
      T recipe = entry.value();
      Identifier id = entry.id().identifier();
      // Get the output from the recipe result display
      ItemStack output = ItemStack.EMPTY;
      var display = entry.value().display();
      if (!display.isEmpty()) {
        var resultDisplay = display.getFirst().result();
        if (resultDisplay instanceof net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay itemDisplay) {
          output = new ItemStack(itemDisplay.item().value());
        } else if (resultDisplay instanceof net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay stackDisplay) {
          //TODO
          output = stackDisplay.stack().create();
        }
      }

      // noinspection ConstantConditions
      if (output == null || output.isEmpty() || entry.value() instanceof CustomRecipe) {
        output = recipe.assemble(recipeInput);
      }

      if (output.isEmpty()) {
        continue;
      }

      if (firstResult == null) {
        firstResult = entry;
      }
      boolean flag = false;

      if (selected == null && this.getSelectedRecipe() != null &&
          this.getSelectedRecipe().id().identifier().equals(id)) {
        selected = entry;
        flag = true;
      }

      if (recipesList.size() < 15 || flag) {
        recipesList.add(new RecipePair(id, output));
      }
    }

    if (selected == null) {
      selected = firstResult;
      this.setSelectedRecipe(selected);
    }
    this.updateRecipesList(recipesList);
    return selected;
  }

  protected void updateRecipesList(SortedSet<IRecipePair> recipesList) {
    this.setRecipesList(recipesList);
    this.sendRecipesListToListeners();
  }

  @Override
  public RecipeHolder<?> getSelectedRecipe() {
    return this.selectedRecipe;
  }

  @Override
  public void setSelectedRecipe(RecipeHolder<?> recipe) {
    this.selectedRecipe = recipe;
  }

  @Nonnull
  @Override
  public SortedSet<IRecipePair> getRecipesList() {
    return this.recipesList;
  }

  @Override
  public void setRecipesList(@Nonnull SortedSet<IRecipePair> recipesList) {
    this.recipesList.clear();
    this.recipesList.addAll(recipesList);
  }

  @Override
  public E getOwner() {
    return this.owner;
  }

  @Override
  public void selectRecipe(@Nonnull RecipeHolder<?> recipe) {
    this.setSelectedRecipe(recipe);
  }

  @Override
  public Collection<ServerPlayer> getListeners() {
    return Collections.unmodifiableCollection(this.listeners.values());
  }

  @Override
  public void addListener(@Nonnull ServerPlayer serverPlayer) {
    this.listeners.put(serverPlayer.getUUID(), serverPlayer);
  }

  @Override
  public void removeListener(@Nonnull ServerPlayer serverPlayer) {
    this.listeners.remove(serverPlayer.getUUID());
  }

  @Override
  public void clearListeners() {
    this.listeners.clear();
  }

  @Override
  public void sendRecipesListToListeners() {
    Identifier resourceLocation =
        this.getSelectedRecipe() != null ? this.getSelectedRecipe().id().identifier() : null;
    Pair<SortedSet<IRecipePair>, Identifier> packetData =
        new Pair<>(this.getRecipesList(), resourceLocation);

    for (ServerPlayer listener : this.getListeners()) {
      PolymorphApi.getInstance().getNetwork()
          .sendRecipesListS2C(listener, packetData.getFirst(), packetData.getSecond());
    }
  }

  @Override
  public void readNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {

    if (compoundTag.contains("SelectedRecipe")) {
      compoundTag.getString("SelectedRecipe").ifPresent(str ->
          this.loadedRecipe = Identifier.tryParse(str));
    }
  }

  @Nonnull
  @Override
  public CompoundTag writeNBT(HolderLookup.Provider provider) {
    CompoundTag nbt = new CompoundTag();

    if (this.selectedRecipe != null) {
      nbt.putString("SelectedRecipe", this.selectedRecipe.id().identifier().toString());
    }
    return nbt;
  }
}
