package com.abadon.minecontrollers.tabs;

import com.abadon.minecontrollers.Minecontrollers;
import com.abadon.minecontrollers.MinecontrollersBlocks;
import com.abadon.minecontrollers.items.MinecontrollersItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Minecontrollers.MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MINECONTROLLERS_TAB = CREATIVE_MODE_TABS.register("minecontrollers_tab", () -> CreativeModeTab.builder()
            .icon(() -> MinecontrollersBlocks.blockItems.get(MinecontrollersBlocks.MICROCONTROLLER_ID).get().getDefaultInstance())
            .title(Component.translatable("itemGroup.minecontrollers"))
            .displayItems((parameters, output) -> {
                output.acceptAll(MinecontrollersBlocks.blockItems.values().stream().map(blockItem -> blockItem.get().getDefaultInstance()).toList()); //registering block items
                output.accept(MinecontrollersItems.DEBUG_DISPLAY_ITEM.get().getDefaultInstance()); //registering items
            }).build());
    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
