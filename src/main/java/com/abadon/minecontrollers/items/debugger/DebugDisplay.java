package com.abadon.minecontrollers.items.debugger;

import com.abadon.minecontrollers.entityblocks.microcontroller.MicrocontrollerBlockEntity;
import com.abadon.minecontrollers.network.packets.DebugSyncPayload;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class DebugDisplay extends Item {

    public enum DebugSettingsMode{
        ONE,
        TEN,
        HUNDRED,
        THOUSAND
    }
    public final String sectionDelimiter = "=================================================";
    protected DebugSettingsMode settingsMode = DebugSettingsMode.ONE;
    public int memoryStep = 1;
    public int dumpSize = 32;
    public int memoryDumpStartAddress = 0;
    public void addAddressDumpStartPoint(){
        memoryDumpStartAddress += memoryStep;
        if(memoryDumpStartAddress > 65535) memoryDumpStartAddress = 65535;
    }
    public void subAddressDumpStartPoint(){
        memoryDumpStartAddress -= memoryStep;
        if(memoryDumpStartAddress < 0) memoryDumpStartAddress = 0;
    }
    public void changeWorkingMode(){
        switch (settingsMode){
            case ONE -> {
                settingsMode = DebugSettingsMode.TEN;
                memoryStep = 10;
            }
            case TEN -> {
                settingsMode = DebugSettingsMode.HUNDRED;
                memoryStep = 100;
            }
            case HUNDRED -> {
                settingsMode = DebugSettingsMode.THOUSAND;
                memoryStep = 1000;
            }
            case THOUSAND -> {
                settingsMode = DebugSettingsMode.ONE;
                memoryStep = 1;
            }
        }
    }
    public DebugDisplay(Properties properties) {
        super(properties);
    }
    public void sendDataToServer(int address) {
        PacketDistributor.sendToServer(new DebugSyncPayload(address));
    }
    private void updateStackData(ItemStack stack, int address, DebugSettingsMode mode) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData ->
                customData.update(tag -> {
                    tag.putInt("address", address);
                    tag.putString("mode", mode.name());
                })
        );
    }

    public void changeWorkingMode(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        DebugSettingsMode currentMode = DebugSettingsMode.valueOf(data.copyTag().getString("mode").isEmpty() ? "ONE" : data.copyTag().getString("mode"));

        DebugSettingsMode nextMode = DebugSettingsMode.values()[(currentMode.ordinal() + 1) % DebugSettingsMode.values().length];

        updateStackData(stack, data.copyTag().getInt("address"), nextMode);
    }
    protected ArrayList<MutableComponent> getRegistersSegment(MicrocontrollerBlockEntity microcontroller){
        ArrayList<MutableComponent> components = new ArrayList<>();
        StringBuilder defaultRegisters = new StringBuilder();
        StringBuilder highBitRegisters = new StringBuilder();
        StringBuilder lowBitRegisters = new StringBuilder();
        StringBuilder segmentRegisters = new StringBuilder();
        defaultRegisters.append("registers:    ").append(microcontroller.getRegistersNames().stream().map(r -> {
            try {
                return r + ": " + String.valueOf(microcontroller.getClass().getField(r).getShort(microcontroller));
            } catch (Exception exception) {
                defaultRegisters.append(" FATAL ERROR: ").append(exception);
            }
            return "";
        }).collect(Collectors.joining("; ")));
        highBitRegisters.append("high registers:    ").append(microcontroller.getHighBitRegistersNames().stream().map(r -> {
            try {
                return r + ": " + String.valueOf(microcontroller.getClass().getField(r).getShort(microcontroller) >>> 8);
            } catch (Exception exception) {
                highBitRegisters.append(" FATAL ERROR: ").append(exception);
            }
            return "";
        }).collect(Collectors.joining("; ")));
        lowBitRegisters.append("low registers:    ").append(microcontroller.getLowBitRegistersNames().stream().map(r -> {
            try {
                return r + ": " + String.valueOf(microcontroller.getClass().getField(r).getShort(microcontroller) & 0xFF);
            } catch (Exception exception) {
                lowBitRegisters.append(" FATAL ERROR: ").append(exception);
            }
            return "";
        }).collect(Collectors.joining("; ")));
        segmentRegisters.append("segment registers:    ").append(microcontroller.getSegmentRegistersNames().stream().map(r -> {
            try {
                return r + ": " + String.valueOf(microcontroller.getClass().getField(r).getShort(microcontroller));
            } catch (Exception exception) {
                segmentRegisters.append(" FATAL ERROR: ").append(exception);
            }
            return "";
        }).collect(Collectors.joining("; ")));
        components.add(MutableComponent.create(new PlainTextContents.LiteralContents(sectionDelimiter)).setStyle(Style.EMPTY.withColor(1798620)));
        components.add(MutableComponent.create(new PlainTextContents.LiteralContents(defaultRegisters.toString())).setStyle(Style.EMPTY.withColor(14243864)));
        components.add(MutableComponent.create(new PlainTextContents.LiteralContents(sectionDelimiter)).setStyle(Style.EMPTY.withColor(1798620)));
        components.add(MutableComponent.create(new PlainTextContents.LiteralContents(highBitRegisters.toString())).setStyle(Style.EMPTY.withColor(14227665)));
        components.add(MutableComponent.create(new PlainTextContents.LiteralContents(sectionDelimiter)).setStyle(Style.EMPTY.withColor(1798620)));
        components.add(MutableComponent.create(new PlainTextContents.LiteralContents(lowBitRegisters.toString())).setStyle(Style.EMPTY.withColor(1624793)));
        components.add(MutableComponent.create(new PlainTextContents.LiteralContents(sectionDelimiter)).setStyle(Style.EMPTY.withColor(1798620)));
        components.add(MutableComponent.create(new PlainTextContents.LiteralContents(segmentRegisters.toString())).setStyle(Style.EMPTY.withColor(15985175)));
        components.add(MutableComponent.create(new PlainTextContents.LiteralContents(sectionDelimiter)).setStyle(Style.EMPTY.withColor(1798620)));
        return components;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        if (player.isShiftKeyDown() && level.isClientSide) {
            changeWorkingMode();
            player.displayClientMessage(MutableComponent.create(new PlainTextContents.LiteralContents(settingsMode.name())), true);
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide) {
            if (level.getBlockEntity(context.getClickedPos()) instanceof MicrocontrollerBlockEntity microcontroller) {
                ArrayList<MutableComponent> debugInfo = new ArrayList<>();
                debugInfo.addAll(getRegistersSegment(microcontroller));
                debugInfo.add(getMemoryDumpInfo(microcontroller));
                for (MutableComponent component : debugInfo)
                    player.displayClientMessage(component, false);
            }
        }

        return InteractionResult.SUCCESS;
    }

    protected MutableComponent getMemoryDumpInfo(MicrocontrollerBlockEntity microcontroller){
        StringBuilder dumpInfoBuilder = new StringBuilder();
        dumpInfoBuilder.append("memory dump: ");
        for(int i = memoryDumpStartAddress; i < memoryDumpStartAddress + dumpSize; i++){
            if(i > 65535) break;
            dumpInfoBuilder.append(Integer.toString(i, 16).toUpperCase()).append(':').append(Integer.toString(microcontroller.getValueByAddress(i) & 0xFF, 16).toUpperCase()).append(' ');
        }
        return MutableComponent.create(new PlainTextContents.LiteralContents(dumpInfoBuilder.toString())).setStyle(Style.EMPTY.withColor(2619179)); // set lime color
    }
}