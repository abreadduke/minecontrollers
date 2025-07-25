package com.abadon.minecontrollers.items.debugger;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;

@OnlyIn(Dist.CLIENT)
public class PlayerInputCatcher {
    public static void debuggerDisplayDumpScroll(final InputEvent.MouseScrollingEvent event){
        final MutableComponent settingMessage = Component.translatable("gui.minecontrollers.dump_message");
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Inventory playerInventory = player.getInventory();
        ItemStack itemStack = playerInventory.items.get(playerInventory.selected);
        Item playerItem = itemStack.getItem();
        if(player != null && playerItem instanceof DebugDisplay display && player.isShiftKeyDown()) {
            if (event.getScrollDelta() > 0){
                display.addAddressDumpStartPoint();
                player.displayClientMessage(MutableComponent.create(new LiteralContents(settingMessage.getString() + String.valueOf(display.memoryDumpStartAddress))), true);
                playerInventory.selected = playerInventory.selected + 1;
            }
            else if (event.getScrollDelta() < 0) {
                display.subAddressDumpStartPoint();
                player.displayClientMessage(MutableComponent.create(new LiteralContents(settingMessage.getString() + String.valueOf(display.memoryDumpStartAddress))), true);
                playerInventory.selected = playerInventory.selected - 1;
            }
            display.sendDataToServer(display.memoryDumpStartAddress);
        }
    }
}
