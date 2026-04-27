package com.abadon.minecontrollers.inventory;

import com.abadon.minecontrollers.Minecontrollers;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModMenu {
    public static final DeferredRegister<MenuType<?>> UI = DeferredRegister.create(Registries.MENU, Minecontrollers.MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<ProgrammerMenu>> PROGRAMMER_MENU_MENU_TYPE = UI.register("programmer_menu", () -> new MenuType(ProgrammerMenu::new, FeatureFlags.VANILLA_SET));
    public static final DeferredHolder<MenuType<?>, MenuType<FormatterMenu>> FORMATTER_MENU_MENU_TYPE = UI.register("formatter_menu", () -> new MenuType(FormatterMenu::new, FeatureFlags.VANILLA_SET));
    public static final DeferredHolder<MenuType<?>, MenuType<AssemblerMenu>> ASSEMBLER_MENU_MENU_TYPE = UI.register("assembler_menu", () -> new MenuType(AssemblerMenu::new, FeatureFlags.VANILLA_SET));
    public static final DeferredHolder<MenuType<?>, MenuType<DumperMenu>> DUMPER_MENU_MENU_TYPE = UI.register("dumper_menu", () -> new MenuType(DumperMenu::new, FeatureFlags.VANILLA_SET));
    public static final DeferredHolder<MenuType<?>, MenuType<AssemblerMenu>> DISASSEMBLER_MENU_MENU_TYPE = UI.register("disassembler_menu", () -> new MenuType(DisassemblerMenu::new, FeatureFlags.VANILLA_SET));
    public static void register(IEventBus eventBus){
        UI.register(eventBus);
    }
    public static void registerScreens(RegisterMenuScreensEvent event){
        event.register(PROGRAMMER_MENU_MENU_TYPE.get(), ProgrammerMenuScreen::new);
        event.register(FORMATTER_MENU_MENU_TYPE.get(), TranslatorMenuScreen::new);
        event.register(ASSEMBLER_MENU_MENU_TYPE.get(), TranslatorMenuScreen::new);
        event.register(DISASSEMBLER_MENU_MENU_TYPE.get(), TranslatorMenuScreen::new);
        event.register(DUMPER_MENU_MENU_TYPE.get(), DumperMenuScreen::new);
    }
}
