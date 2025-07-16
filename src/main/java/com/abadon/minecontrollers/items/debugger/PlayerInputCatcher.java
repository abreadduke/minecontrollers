package com.abadon.minecontrollers.items.debugger;

import com.abadon.minecontrollers.items.MinecontrollersItems;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.InputEvent;

public class PlayerInputCatcher {
    public static void debuggerDisplayDumpScroll(final InputEvent.MouseScrollingEvent event){
        final MutableComponent settingMessage = Component.translatable("gui.minecontrollers.dump_message");
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Inventory playerInventory = player.getInventory();
        Item playerItem = playerInventory.items.get(playerInventory.selected).getItem();
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
        }
    }
}
