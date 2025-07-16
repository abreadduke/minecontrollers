package com.abadon.minecontrollers.inventory;

import com.abadon.minecontrollers.Minecontrollers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ProgrammerMenuScreen extends AbstractContainerScreen<ProgrammerMenu> {
    public static final ResourceLocation PROGRAMMER_MENU_TEXTURE = ResourceLocation.fromNamespaceAndPath(Minecontrollers.MODID, "textures/gui/container/programmer_menu.png");
    public ProgrammerMenuScreen(ProgrammerMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
    }
    public void render(GuiGraphics guiGraphics, int p1, int p2, float p3) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, p1, p2 ,p3);
        this.renderTooltip(guiGraphics, p1, p2);
    }
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        int $$4 = (this.width - this.imageWidth) / 2;
        int $$5 = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(PROGRAMMER_MENU_TEXTURE, $$4, $$5, 0, 0, this.imageWidth, this.imageHeight);
    }
}
