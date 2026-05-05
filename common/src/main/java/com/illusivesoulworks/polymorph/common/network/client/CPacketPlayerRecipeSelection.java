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

package com.illusivesoulworks.polymorph.common.network.client;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IPlayerRecipeData;
import com.illusivesoulworks.polymorph.common.integration.PolymorphIntegrations;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.crafting.Recipe;

import javax.annotation.Nonnull;

public record CPacketPlayerRecipeSelection(Identifier recipe) implements CustomPacketPayload {

  public static final Type<CPacketPlayerRecipeSelection> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(PolymorphApi.MOD_ID, "player_recipe_selection"));
  public static final StreamCodec<FriendlyByteBuf, CPacketPlayerRecipeSelection> STREAM_CODEC =
      StreamCodec.composite(
          Identifier.STREAM_CODEC,
          CPacketPlayerRecipeSelection::recipe,
          CPacketPlayerRecipeSelection::new);

  public static void handle(CPacketPlayerRecipeSelection packet, ServerPlayer player) {
    AbstractContainerMenu container = player.containerMenu;
    ServerLevel serverLevel = (ServerLevel) player.level();
    ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, packet.recipe);
    serverLevel.recipeAccess().byKey(recipeKey).ifPresent(recipe -> {
      PolymorphApi api = PolymorphApi.getInstance();
      IPlayerRecipeData recipeData = api.getPlayerRecipeData(player);

      if (recipeData != null) {
        recipeData.selectRecipe(recipe);
      }
      PolymorphIntegrations.selectRecipe(container, recipe);
      container.slotsChanged(player.getInventory());

      if (container instanceof ItemCombinerMenu) {
        ((ItemCombinerMenu) container).createResult();
        container.broadcastChanges();
        api.getNetwork().sendUpdatePreviewS2C(player);
      }
    });
  }

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
