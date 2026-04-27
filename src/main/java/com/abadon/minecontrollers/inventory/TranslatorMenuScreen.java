package com.abadon.minecontrollers.inventory;

import com.abadon.minecontrollers.Minecontrollers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TranslatorMenuScreen extends AbstractContainerScreen<TranslatorBlockMenu> {
    private static ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Minecontrollers.MODID, "textures/gui/container/formatter_menu.png");

    public TranslatorMenuScreen(TranslatorBlockMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float p1, int p2, int p3) {
        int i = this.leftPos;
        int j = this.topPos;
        guiGraphics.blit(this.texture, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }
}
