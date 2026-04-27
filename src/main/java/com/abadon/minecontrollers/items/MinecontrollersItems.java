package com.abadon.minecontrollers.items;

import com.abadon.minecontrollers.Minecontrollers;
import com.abadon.minecontrollers.items.debugger.DebugDisplay;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MinecontrollersItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Minecontrollers.MODID);

    public static final DeferredHolder<Item, DebugDisplay> DEBUG_DISPLAY_ITEM = ITEMS.register(
            "controller_debug_display",
            () -> new DebugDisplay(new Item.Properties().stacksTo(1))
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}