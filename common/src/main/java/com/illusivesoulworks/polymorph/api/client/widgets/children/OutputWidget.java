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

package com.illusivesoulworks.polymorph.api.client.widgets.children;

import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class OutputWidget extends AbstractWidget {

  private final ItemStack output;
  private final Identifier resourceLocation;
  private final Pair<WidgetSprites, WidgetSprites> sprites;
  private boolean highlighted = false;

  public OutputWidget(Pair<WidgetSprites, WidgetSprites> sprites, IRecipePair recipePair) {
    super(0, 0, 25, 25, Component.empty());
    this.output = recipePair.getOutput();
    this.resourceLocation = recipePair.getResourceLocation();
    this.sprites = sprites;
  }

  @Override
  protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
    Minecraft minecraft = Minecraft.getInstance();
    WidgetSprites sprite = this.highlighted ? this.sprites.getSecond() : this.sprites.getFirst();
    Identifier texture = sprite.enabled();

    if (this.getX() + 25 > mouseX && this.getX() <= mouseX &&
            this.getY() + 25 > mouseY && this.getY() <= mouseY) {
      texture = sprite.enabledFocused();
    }
    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), this.width, this.height);
    int k = 4;
    guiGraphics.item(this.getOutput(), this.getX() + k, this.getY() + k);
    guiGraphics.itemDecorations(minecraft.font, this.getOutput(), this.getX() + k,
            this.getY() + k);
  }

  public ItemStack getOutput() {
    return this.output;
  }

  public Identifier getResourceLocation() {
    return this.resourceLocation;
  }

  public void setHighlighted(boolean highlighted) {
    this.highlighted = highlighted;
  }

  @Override
  public int getWidth() {
    return 25;
  }

  @Override
  protected void updateWidgetNarration(@Nonnull NarrationElementOutput var1) {

  }

}
