package com.abadon.minecontrollers.items;

import com.abadon.minecontrollers.Minecontrollers;
import com.abadon.minecontrollers.items.debugger.DebugDisplay;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class MinecontrollersItems {
    public static DeferredRegister<Item> items = DeferredRegister.create(Registries.ITEM, Minecontrollers.MODID);
    public static RegistryObject<DebugDisplay> DEBUG_DISPLAY_ITEM = items.register("controller_debug_display", () -> new DebugDisplay(new Item.Properties().stacksTo(1)));
    public static void register(IEventBus eventBus){
        items.register(eventBus);
    }
}
