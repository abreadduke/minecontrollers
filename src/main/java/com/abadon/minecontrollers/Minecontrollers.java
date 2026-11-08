package com.abadon.minecontrollers;

import com.abadon.minecontrollers.interrupts.MinecontrollersInterrupts;
import com.abadon.minecontrollers.inventory.DumperMenu;
import com.abadon.minecontrollers.inventory.ModMenu;
import com.abadon.minecontrollers.items.MinecontrollersItems;
import com.abadon.minecontrollers.items.debugger.DebugDisplay;
import com.abadon.minecontrollers.items.debugger.PlayerInputCatcher;
import com.abadon.minecontrollers.network.packets.DebugSyncPayload;
import com.abadon.minecontrollers.network.packets.DebugSyncResponsePayload;
import com.abadon.minecontrollers.network.packets.DumperPayload;
import com.abadon.minecontrollers.tabs.ModTab;
import com.mojang.logging.LogUtils;
import commoble.morered.api.MoreRedAPI;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Minecontrollers.MODID)
public class Minecontrollers
{
    public static final String MODID = "minecontrollers";
    private static final Logger LOGGER = LogUtils.getLogger();
    protected void register(IEventBus modEventBus){
        MinecontrollersBlocks.register(modEventBus);
        MinecontrollersItems.register(modEventBus);
        ModTab.register(modEventBus);
        ModMenu.register(modEventBus);
    }
    public Minecontrollers(IEventBus modEventBus, ModContainer context)
    {
        ModLoadingContext modContext = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        LOGGER.info("registration custom blocks/items");
        register(modEventBus);
        modEventBus.addListener(Minecontrollers::onRegisterCapabilities);
        MinecontrollersInterrupts.register();
    }

    // Runs on both sides — registers MoreRed capabilities for the block entities
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(MoreRedAPI.CHANNELED_POWER_CAPABILITY, MinecontrollersBlocks.PROGRAMMER_BE.get(), (be, side) -> be.getChanneledPower(side));
        event.registerBlockEntity(MoreRedAPI.CHANNELED_POWER_CAPABILITY, MinecontrollersBlocks.MICROCONTROLLER_BE.get(), (be, side) -> be.getChanneledPower(side));
    }

    @EventBusSubscriber(modid = Minecontrollers.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class CommonModEvents
    {
        @SubscribeEvent
        public static void registerNetworking(RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar(Minecontrollers.MODID);

            registrar.playToServer(
                    DebugSyncPayload.TYPE,
                    DebugSyncPayload.STREAM_CODEC,
                    (payload, context) -> {
                        context.enqueueWork(() -> {
                            ServerPlayer player = (ServerPlayer)context.player();
                            if (player.getMainHandItem().getItem() instanceof DebugDisplay item) {
                                ItemStack stack = player.getMainHandItem();

                                // Сохраняем новый адрес в кастомные данные предмета
                                stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData ->
                                        customData.update(tag -> tag.putInt("address", payload.address()))
                                );

                                // Отправляем подтверждение на клиент
                                PacketDistributor.sendToPlayer(player, new DebugSyncResponsePayload(payload.address()));
                            }
                        });
                    }
            );
            registrar.playToClient(
                    DebugSyncResponsePayload.TYPE,
                    DebugSyncResponsePayload.STREAM_CODEC,
                    (payload, context) -> {
                        if (context.player().getMainHandItem().getItem() instanceof DebugDisplay item) {
                            ItemStack stack = context.player().getMainHandItem();

                            // Обновляем адрес на клиенте
                            stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData ->
                                    customData.update(tag -> tag.putInt("address", payload.address()))
                            );
                        }
                    }
            );
            registrar.playToServer(
                    DumperPayload.TYPE,
                    DumperPayload.STREAM_CODEC,
                    (payload, context) -> {
                        context.enqueueWork(() -> {
                            if (context.player().containerMenu instanceof DumperMenu dumperMenu) {
                                dumperMenu.doDump(payload.from(), payload.to());
                            }
                        });
                    }
            );
        }
    }

    @EventBusSubscriber(modid = Minecontrollers.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Safe: executed only on the client dist, so loading the client-only
            // PlayerInputCatcher class (which references Minecraft) won't crash
            // a dedicated server.
            NeoForge.EVENT_BUS.addListener(PlayerInputCatcher::debuggerDisplayDumpScroll);

            MinecontrollersBlocks.registerSkullBlocks();
        }
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            ModMenu.registerScreens(event);
        }
    }
}
