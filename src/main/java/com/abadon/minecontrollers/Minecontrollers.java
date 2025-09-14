package com.abadon.minecontrollers;

import com.abadon.minecontrollers.interrupts.MinecontrollersInterrupts;
import com.abadon.minecontrollers.inventory.DumperMenu;
import com.abadon.minecontrollers.inventory.ModMenu;
import com.abadon.minecontrollers.items.MinecontrollersItems;
import com.abadon.minecontrollers.items.debugger.PlayerInputCatcher;
import com.abadon.minecontrollers.tabs.ModTab;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Minecontrollers.MODID)
public class Minecontrollers
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "minecontrollers";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    protected void register(IEventBus modEventBus){
        MinecontrollersBlocks.register(modEventBus);
        MinecontrollersItems.register(modEventBus);
        ModTab.register(modEventBus);
        ModMenu.register(modEventBus);
    }
    public Minecontrollers(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        ModLoadingContext modContext = ModLoadingContext.get();
        modContext.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);

        // Register the commonSetup method for modloading
        //modEventBus.addListener(this::commonSetup);
        //modEventBus.addListener(PlayerInputCatcher::debuggerDisplayDumpScroll);
        LOGGER.info("registration custom blocks/items");
        register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        MinecontrollersInterrupts.register();
        DumperMenu.registerNetworkChannel();
    }

    //private void commonSetup(final FMLCommonSetupEvent event)
    //{
    //    // Some common setup code
    //    LOGGER.info("HELLO FROM COMMON SETUP");
    //    if (Config.logDirtBlock)
    //        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
    //    LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
    //    Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    //}

    // Add the example block item to the building blocks tab
    //private void addCreative(BuildCreativeModeTabContentsEvent event)
    //{
    //    if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
    //        event.accept(EXAMPLE_BLOCK_ITEM);
    //}

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    //@SubscribeEvent
    //public void onServerStarting(ServerStartingEvent event)
    //{
    //
    //}

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            MinecraftForge.EVENT_BUS.addListener(PlayerInputCatcher::debuggerDisplayDumpScroll);
            ModMenu.registerScreens();
            MinecontrollersBlocks.registerSkullBlocks();
        }
    }
}
