package com.abadon.minecontrollers.inventory;

import com.abadon.minecontrollers.Minecontrollers;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenu {
    public static final DeferredRegister<MenuType<?>> UI = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Minecontrollers.MODID);
    public static final RegistryObject<MenuType<ProgrammerMenu>> PROGRAMMER_MENU_MENU_TYPE = UI.register("programmer_menu", () -> new MenuType(ProgrammerMenu::new, FeatureFlags.VANILLA_SET));
    public static final RegistryObject<MenuType<FormatterMenu>> FORMATTER_MENU_MENU_TYPE = UI.register("formatter_menu", () -> new MenuType(FormatterMenu::new, FeatureFlags.VANILLA_SET));
    public static final RegistryObject<MenuType<AssemblerMenu>> ASSEMBLER_MENU_MENU_TYPE = UI.register("assembler_menu", () -> new MenuType(AssemblerMenu::new, FeatureFlags.VANILLA_SET));
    public static void register(IEventBus eventBus){
        //MenuScreens.register(PROGRAMMER_MENU_MENU_TYPE.get(), ProgrammerMenuScreen::new);
        //ForgeRegistries.MENU_TYPES.register(ResourceLocation.fromNamespaceAndPath(Minecontrollers.MODID, "textures/gui/container/programmer_menu.png"), PROGRAMMER_MENU_MENU_TYPE.get());
        UI.register(eventBus);
    }
    public static void registerScreens(){
        MenuScreens.register(PROGRAMMER_MENU_MENU_TYPE.get(), ProgrammerMenuScreen::new);
        MenuScreens.register(FORMATTER_MENU_MENU_TYPE.get(), TranslatorMenuScreen::new);
        MenuScreens.register(ASSEMBLER_MENU_MENU_TYPE.get(), TranslatorMenuScreen::new);
    }
}
