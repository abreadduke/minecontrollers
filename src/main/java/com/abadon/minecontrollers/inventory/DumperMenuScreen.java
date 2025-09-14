package com.abadon.minecontrollers.inventory;

import com.abadon.minecontrollers.Minecontrollers;
import com.abadon.minecontrollers.network.packets.DumperPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DumperMenuScreen extends AbstractContainerScreen<DumperMenu> {
    public static final ResourceLocation DUMPER_MENU_TEXTURE = ResourceLocation.fromNamespaceAndPath(Minecontrollers.MODID, "textures/gui/container/dumper_menu.png");
    protected EditBox startValueField;
    protected EditBox endValueField;
    protected StringWidget startValueText;
    protected StringWidget endValueText;
    protected ImageButton dumpButton;
    protected ImageWidget pressedDumpButton;
    private float activeButtonTicks = 0;
    private boolean isDumpButtonActive = false;
    protected String previousStartValue = "0000";
    protected String previousEndValue = "0000";
    protected int startValue = 0;
    protected int endValue = 0;
    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        startValueField = new EditBox(this.font, i + 8, j + 27, 33, 12, Component.literal("0000"));
        endValueField = new EditBox(this.font, i + 8, j + 53, 33, 12, Component.literal("0000"));

        startValueText = new StringWidget((this.width - this.imageWidth) + 43, (this.height - this.imageHeight) + 43, Component.translatable("container.minecontrollers.dumper.begin_text"), font);
        endValueText = new StringWidget((this.width - this.imageWidth) + 43, (this.height - this.imageHeight) + 95, Component.translatable("container.minecontrollers.dumper.bytes_text"), font);

        dumpButton = new ImageButton(i + 76, j + 53, 24, 24, 0, 0, 24, ResourceLocation.fromNamespaceAndPath(Minecontrollers.MODID, "textures/gui/container/start_button.png"), 24, 24, (Button button) -> {
            button.active = false;
            isDumpButtonActive = true;
            menu.sendDumpPacket(new DumperPacket(startValue, endValue));
        });
        pressedDumpButton = new ImageWidget(i + 76, j + 53, 24, 24, ResourceLocation.fromNamespaceAndPath(Minecontrollers.MODID, "textures/gui/container/pressed_start_button.png"));

        startValueField.setCanLoseFocus(false);
        startValueField.setTextColor(-1);
        startValueField.setTextColorUneditable(-1);
        startValueField.setBordered(true);
        startValueField.setMaxLength(4);
        startValueField.setValue(previousStartValue);
        startValueField.setResponder(this::onStartValueChanged);
        startValueField.setEditable(true);
        startValueText.setColor(7237230);

        endValueField.setCanLoseFocus(false);
        endValueField.setTextColor(-1);
        endValueField.setTextColorUneditable(-1);
        endValueField.setBordered(true);
        endValueField.setMaxLength(4);
        endValueField.setValue(previousEndValue);
        endValueField.setResponder(this::onEndValueChanged);
        endValueField.setEditable(true);
        endValueText.setColor(7237230);

        addWidget(this.startValueField);
        addWidget(this.endValueField);
    }
    protected void onStartValueChanged(String value){
        try{
            startValue = Integer.valueOf(value, 16);
            previousStartValue = value;
        } catch (NumberFormatException exception){
            startValueField.setValue(previousStartValue);
        }
    }
    protected void onEndValueChanged(String value){
        try{
            endValue = Integer.valueOf(value, 16);
            previousEndValue = value;
        } catch (NumberFormatException exception){
            endValueField.setValue(previousEndValue);
        }
    }
    public DumperMenuScreen(DumperMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
    }

    @Override
    public boolean mouseClicked(double x, double y, int s) {
        dumpButton.mouseClicked(x, y, s);
        return super.mouseClicked(x, y, s);
    }

    @Override
    public boolean keyPressed(int p_97878_, int p_97879_, int p_97880_) {
        if (p_97878_ == 69) {
            return true;
        } else return super.keyPressed(p_97878_, p_97879_, p_97880_);
    }
    @Override
    public void render(GuiGraphics guiGraphics, int p1, int p2, float p3) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, p1, p2 ,p3);
        this.renderTooltip(guiGraphics, p1, p2);
    }
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        int $$4 = (this.width - this.imageWidth) / 2;
        int $$5 = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(DUMPER_MENU_TEXTURE, $$4, $$5, 0, 0, this.imageWidth, this.imageHeight);
        renderFg(guiGraphics, v, i, i1);
    }

    public void renderFg(GuiGraphics guiGraphics, float v, int i, int i1) {
        startValueField.render(guiGraphics, i, i1, v);
        endValueField.render(guiGraphics, i, i1, v);
        startValueText.renderWidget(guiGraphics, i, i1, v);
        endValueText.renderWidget(guiGraphics, i, i1, v);
        if(!isDumpButtonActive)
            dumpButton.renderWidget(guiGraphics, i, i1, v);
        else {
            pressedDumpButton.renderWidget(guiGraphics, i, i1, v);
            activeButtonTicks += Minecraft.getInstance().getDeltaFrameTime();
            if(activeButtonTicks > 1.5){
                activeButtonTicks = 0;
                isDumpButtonActive = false;
                dumpButton.active = true;
            }
        }
    }
}
