package com.abadon.minecontrollers.items.debugger;

import com.abadon.minecontrollers.entityblocks.microprocessor.MicrocontrollerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.network.chat.Style;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class DebugDisplay extends Item {
    public enum DebugSettingsMode{
        ONE,
        TEN,
        HUNDRED,
        THOUSAND
    }
    protected DebugSettingsMode settingsMode = DebugSettingsMode.ONE;
    public DebugDisplay(Properties p_41383_) {
        super(p_41383_);
    }
    public int memoryStep = 1;
    public int dumpSize = 32;
    public int memoryDumpStartAddress = 0;
    public String sectionDelimiter = "=================================================";
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
        components.add(MutableComponent.create(new LiteralContents(sectionDelimiter)).setStyle(Style.EMPTY.withColor(1798620)));
        components.add(MutableComponent.create(new LiteralContents(defaultRegisters.toString())).setStyle(Style.EMPTY.withColor(14243864)));
        components.add(MutableComponent.create(new LiteralContents(sectionDelimiter)).setStyle(Style.EMPTY.withColor(1798620)));
        components.add(MutableComponent.create(new LiteralContents(highBitRegisters.toString())).setStyle(Style.EMPTY.withColor(14227665)));
        components.add(MutableComponent.create(new LiteralContents(sectionDelimiter)).setStyle(Style.EMPTY.withColor(1798620)));
        components.add(MutableComponent.create(new LiteralContents(lowBitRegisters.toString())).setStyle(Style.EMPTY.withColor(1624793)));
        components.add(MutableComponent.create(new LiteralContents(sectionDelimiter)).setStyle(Style.EMPTY.withColor(1798620)));
        components.add(MutableComponent.create(new LiteralContents(segmentRegisters.toString())).setStyle(Style.EMPTY.withColor(15985175)));
        components.add(MutableComponent.create(new LiteralContents(sectionDelimiter)).setStyle(Style.EMPTY.withColor(1798620)));
        return components;
    }
    protected MutableComponent getMemoryDumpInfo(MicrocontrollerBlockEntity microcontroller){
        StringBuilder dumpInfoBuilder = new StringBuilder();
        dumpInfoBuilder.append("memory dump: ");
        for(int i = memoryDumpStartAddress; i < memoryDumpStartAddress + dumpSize; i++){
            if(i > 65535) break;
            dumpInfoBuilder.append(Integer.toString(i, 16)).append(':').append(Integer.toString(microcontroller.getValueByAddress(i) & 0xFF, 16)).append(' ');
        }
        return MutableComponent.create(new LiteralContents(dumpInfoBuilder.toString())).setStyle(Style.EMPTY.withColor(2619179)); // set lime color
    }
    private byte usageCounts = 0;
    @OnlyIn(Dist.CLIENT)
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if(++usageCounts % 2 == 0) usageCounts = 0;
        else return super.onItemUseFirst(stack, context);

        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player != null) {
            if(player.isShiftKeyDown()){
                changeWorkingMode();
                player.displayClientMessage(MutableComponent.create(new LiteralContents(settingsMode.name())), true);
            }
            if (level.getBlockEntity(blockPos) instanceof MicrocontrollerBlockEntity microcontroller) {
                ArrayList<MutableComponent> debugInfo = new ArrayList<>();
                debugInfo.addAll(getRegistersSegment(microcontroller));
                debugInfo.add(getMemoryDumpInfo(microcontroller));
                for (MutableComponent component : debugInfo)
                    player.displayClientMessage(component, false);
            }
            return InteractionResult.SUCCESS;
        }
        return super.onItemUseFirst(stack, context);
    }
}
