package com.abadon.minecontrollers.entityblocks;

import com.abadon.minecontrollers.Minecontrollers;
import com.abadon.minecontrollers.blocks.assembler.Assembler;
import com.abadon.minecontrollers.entityblocks.microcontroller.MicrocontrollerBlock;
import com.abadon.minecontrollers.entityblocks.microcontroller.MicrocontrollerBlockEntity;
import com.abadon.minecontrollers.entityblocks.programmer.Programmer;
import com.abadon.minecontrollers.entityblocks.programmer.ProgrammerBlockEntity;
import com.mojang.logging.LogUtils;
import commoble.morered.api.MoreRedAPI;
import commoble.morered.api.WireConnector;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PlayerHeadBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import com.abadon.minecontrollers.blocks.formatter.Formatter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class MinecontrollersBlocks {
    public static HashMap<String, RegistryObject<? extends Item>> blockItems = new HashMap<>();
    public static <B extends Block, I extends BlockItem>RegistryObject<B> registerBlockItem(String blockId, DeferredRegister<Item> items, DeferredRegister<Block> blocks, Function<? super B, ? extends I> itemFactory, Supplier<? extends B> blockFactory){
        RegistryObject<B> regb = blocks.register(blockId, blockFactory);
        RegistryObject<I> item = items.register(blockId, () -> itemFactory.apply(regb.get()));
        blockItems.put(blockId, item);
        return regb;
    }
    private static void applySkullTexture(ItemStack itemStack, String texture_data){
        CompoundTag skullOwnerTag = new CompoundTag();
        IntArrayTag skullOwnerId = new IntArrayTag(new int[]{2080793942, -524468218, -1541115779, 1949756395});
        CompoundTag skullOwnerProperties = new CompoundTag();
        ListTag textures = new ListTag();
        CompoundTag texture = new CompoundTag();
        texture.put("Value", StringTag.valueOf(texture_data));
        textures.add(texture);
        skullOwnerProperties.put("textures", textures);
        skullOwnerTag.put("Id", skullOwnerId);
        skullOwnerTag.put("Properties", skullOwnerProperties);
        itemStack.addTagElement("SkullOwner", skullOwnerTag);
    }
    public static DeferredRegister<Item> items = DeferredRegister.create(Registries.ITEM, Minecontrollers.MODID);
    public static DeferredRegister<BlockEntityType<?>> blockEntities = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Minecontrollers.MODID);
    public static DeferredRegister<Block> blocks = DeferredRegister.create(Registries.BLOCK, Minecontrollers.MODID);
    public static final String MICROCONTROLLER_ID = "microcontroller";
    public static final String PROGRAMMER_ID = "bundled_programmer";
    public static final String FORMATTER_ID = "formatter";
    public static final String ASSEMBLER_ID = "assembler";
    public static final SoundType defaultComponentSound = SoundType.WOOD;
    public static final int defaultDestroyTime = 2;
    public static final int defaultStrength = 3;
    public static RegistryObject<MicrocontrollerBlock> MICROCONTROLLER_BLOCK = registerBlockItem(MICROCONTROLLER_ID, items, blocks, b -> new BlockItem(b, new Item.Properties()), () -> new MicrocontrollerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).strength(0.0F).sound(SoundType.WOOD)));
    public static RegistryObject<BlockEntityType<MicrocontrollerBlockEntity>> MICROCONTROLLER_BE = blockEntities.register(MICROCONTROLLER_ID, () -> Builder.of(MicrocontrollerBlockEntity::new, new Block[]{MICROCONTROLLER_BLOCK.get()}).build(null));
    public static RegistryObject<Programmer> PROGRAMMER = registerBlockItem(PROGRAMMER_ID, items, blocks, b -> new BlockItem(b, new Item.Properties()), () -> new Programmer(BlockBehaviour.Properties.of().strength(defaultDestroyTime, defaultStrength).mapColor(MapColor.WOOD)));
    public static RegistryObject<BlockEntityType<ProgrammerBlockEntity>> PROGRAMMER_BE = blockEntities.register(PROGRAMMER_ID, () -> Builder.of(ProgrammerBlockEntity::new, new Block[]{PROGRAMMER.get()}).build(null));
    public static RegistryObject<Formatter> FORMATTER_BLOCK = registerBlockItem(FORMATTER_ID, items,  blocks, b -> new BlockItem(b, new Item.Properties()), () -> new Formatter(BlockBehaviour.Properties.of().strength(defaultDestroyTime, defaultStrength).sound(defaultComponentSound)));
    public static RegistryObject<Assembler> ASSEMBLER_BLOCK = registerBlockItem(ASSEMBLER_ID, items, blocks, b -> new BlockItem(b, new Item.Properties()), () -> new Assembler(BlockBehaviour.Properties.of().strength(defaultDestroyTime, defaultStrength).sound(defaultComponentSound)));
    public static final String REDSTONE_CORE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODg0ZTkyNDg3YzY3NDk5OTViNzk3MzdiOGE5ZWI0YzQzOTU0Nzk3YTZkZDZjZDliNGVmY2UxN2NmNDc1ODQ2In19fQ==";
    public static final String REDSTONE_CONTROLLER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWFlNTY0MWY3YmI5ZTg3NWNkNDEyZDRiYTIwYjIyZjg3NjczYTZmMWJlMzI3OGE2MzFjODgxZDg0YzA0NDZmYiJ9fX0=";
    public static RegistryObject<PlayerHeadBlock> REDSTONE_CORE = registerBlockItem("redstone_core", items, blocks, b -> new BlockItem(b, new Item.Properties()){
        @Override
        public String getCreatorModId(ItemStack itemStack)
        {
            applySkullTexture(itemStack, REDSTONE_CORE_TEXTURE);
            return super.getCreatorModId(itemStack);
        }
        @Override
        public ItemStack getDefaultInstance() {
            ItemStack itemStack = super.getDefaultInstance();
            applySkullTexture(itemStack, REDSTONE_CORE_TEXTURE);
            return itemStack;
        }

        @Override
        public int getMaxStackSize(ItemStack stack){
            applySkullTexture(stack, REDSTONE_CORE_TEXTURE);
            return super.getMaxStackSize(stack);
        }
    }, () -> new PlayerHeadBlock(BlockBehaviour.Properties.of().strength(1.0F).pushReaction(PushReaction.DESTROY)));
    public static RegistryObject<PlayerHeadBlock> REDSTONE_CONTROLLER = registerBlockItem("redstone_controller", items, blocks, b -> new BlockItem(b, new Item.Properties()){
        @Override
        public String getCreatorModId(ItemStack itemStack)
        {
            applySkullTexture(itemStack, REDSTONE_CONTROLLER_TEXTURE);
            return super.getCreatorModId(itemStack);
        }
        @Override
        public ItemStack getDefaultInstance() {
            ItemStack itemStack = super.getDefaultInstance();
            applySkullTexture(itemStack, REDSTONE_CONTROLLER_TEXTURE);
            return itemStack;
        }
        @Override
        public int getMaxStackSize(ItemStack stack){
            applySkullTexture(stack, REDSTONE_CONTROLLER_TEXTURE);
            return super.getMaxStackSize(stack);
        }
    }, () -> new PlayerHeadBlock(BlockBehaviour.Properties.of().strength(1.0F).pushReaction(PushReaction.DESTROY)));
    public static void registerSkullBlocks(){
        for(Field validBlocksField : BlockEntityType.SKULL.getClass().getDeclaredFields()){
            boolean acces = validBlocksField.isAccessible();
            boolean wasRegistered = false;
            try{
                validBlocksField.setAccessible(true);
                if(validBlocksField.get(BlockEntityType.SKULL) instanceof Set<?> validBlocks && validBlocks.contains(Blocks.PLAYER_HEAD)){
                    HashSet<Block> newValidBlocks = new HashSet<>();
                    newValidBlocks.addAll((Set<Block>)validBlocks);
                    newValidBlocks.add(MinecontrollersBlocks.REDSTONE_CORE.get());
                    newValidBlocks.add(MinecontrollersBlocks.REDSTONE_CONTROLLER.get());
                    validBlocksField.set(BlockEntityType.SKULL, newValidBlocks);
                    wasRegistered = true;
                }
            } catch (IllegalAccessException exception){

            } finally {
                validBlocksField.setAccessible(acces);
            }
            if(!wasRegistered) LogUtils.getLogger().error("Minecontrollers: registerSkullBlocks error");
        }
    }
    public static void register(IEventBus eventBus){
        eventBus.addListener(EventPriority.HIGH, MinecontrollersBlocks::HighPriorityCommonSetup);
        items.register(eventBus);
        blocks.register(eventBus);
        blockEntities.register(eventBus);
    }
    public static void HighPriorityCommonSetup(FMLCommonSetupEvent event){
        MicrocontrollerBlock microcontrollerBlock = MICROCONTROLLER_BLOCK.get();
        Programmer programmer = PROGRAMMER.get();
        MoreRedAPI.getCableConnectabilityRegistry().put(microcontrollerBlock, (WireConnector)(world, thisPos, thisState, wirePos, wireState, wireFace, directionToWire) -> microcontrollerBlock.canConnectToAdjacentCable(world, thisPos, thisState, wirePos, wireState, wireFace, directionToWire));
        MoreRedAPI.getCableConnectabilityRegistry().put(programmer, (WireConnector)(world, thisPos, thisState, wirePos, wireState, wireFace, directionToWire) -> programmer.canConnectToAdjacentCable(world, thisPos, thisState, wirePos, wireState, wireFace, directionToWire));
    }
}
