package com.abadon.minecontrollers;

import com.abadon.minecontrollers.blocks.assembler.Assembler;
import com.abadon.minecontrollers.blocks.disassembler.Disassembler;
import com.abadon.minecontrollers.blocks.dumper.Dumper;
import com.abadon.minecontrollers.blocks.formatter.Formatter;
import com.abadon.minecontrollers.entityblocks.microcontroller.MicrocontrollerBlock;
import com.abadon.minecontrollers.entityblocks.microcontroller.MicrocontrollerBlockEntity;
import com.abadon.minecontrollers.entityblocks.programmer.Programmer;
import com.abadon.minecontrollers.entityblocks.programmer.ProgrammerBlockEntity;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.logging.LogUtils;
import commoble.morered.api.MoreRedAPI;
import commoble.morered.api.WireConnector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

public class MinecontrollersBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Minecontrollers.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Minecontrollers.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Minecontrollers.MODID);
    public static final Map<String, DeferredHolder<Item, ? extends Item>> blockItems = new HashMap<>();
    public static final Set<DeferredHolder<Block, ? extends Block>> skullBlocks = new HashSet<>();

    public static <T extends Block> DeferredHolder<Block, T> registerBlockItem(String id, Supplier<T> blockSupplier) {
        DeferredHolder<Block, T> block = BLOCKS.register(id, blockSupplier);
        DeferredHolder<Item, BlockItem> item = ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
        blockItems.put(id, item);
        return block;
    }

    public static DeferredHolder<Block, PlayerHeadBlock> registerSkullBlockItem(String blockId, String texture) {
        DeferredHolder<Block, PlayerHeadBlock> skullBlock = BLOCKS.register(blockId,
                () -> new PlayerHeadBlock(BlockBehaviour.Properties.of().strength(1.0F).pushReaction(PushReaction.DESTROY)));

        BLOCKS.register(blockId + "_wall",
                () -> new PlayerWallHeadBlock(BlockBehaviour.Properties.of().strength(1.0F).pushReaction(PushReaction.DESTROY).dropsLike(skullBlock.get())) {
                    @Override
                    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
                        skullBlock.get().setPlacedBy(level, pos, state, entity, stack);
                    }
                });
        DeferredHolder<Block, ? extends Block> wallBlockHolder = BLOCKS.getEntries().stream()
                .filter(holder -> holder.getId().getPath().equals(blockId + "_wall"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Block not found: " + blockId + "_wall"));
        DeferredHolder<Item, PlayerHeadItem> item = ITEMS.register(blockId, () -> new PlayerHeadItem(skullBlock.get(), wallBlockHolder.get(), new Item.Properties()) {
            @Override
            public ItemStack getDefaultInstance() {
                ItemStack stack = super.getDefaultInstance();
                applySkullTexture(stack, texture);
                return stack;
            }
        });

        blockItems.put(blockId, item);
        skullBlocks.add(skullBlock);
        return skullBlock;
    }

    private static void applySkullTexture(ItemStack stack, String textureData) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "CustomSkull");
        profile.getProperties().put("textures", new Property("textures", textureData));
        stack.set(DataComponents.PROFILE, new ResolvableProfile(profile));
    }

    public static final SoundType defaultComponentSound = SoundType.WOOD;
    public static final int defaultDestroyTime = 2;
    public static final int defaultStrength = 3;
    public static final String MICROCONTROLLER_ID = "microcontroller";
    public static final String PROGRAMMER_ID = "bundled_programmer";
    public static final String FORMATTER_ID = "formatter";
    public static final String ASSEMBLER_ID = "assembler";
    public static final String DUMPER_ID = "dumper";
    public static final String DISASSEMBLER_ID = "disassembler";

    public static final String REDSTONE_CORE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODg0ZTkyNDg3YzY3NDk5OTViNzk3MzdiOGE5ZWI0YzQzOTU0Nzk3YTZkZDZjZDliNGVmY2UxN2NmNDc1ODQ2In19fQ==";
    public static final String REDSTONE_CONTROLLER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWFlNTY0MWY3YmI5ZTg3NWNkNDEyZDRiYTIwYjIyZjg3NjczYTZmMWJlMzI3OGE2MzFjODgxZDg0YzA0NDZmYiJ9fX0=";
    public static DeferredHolder<Block, PlayerHeadBlock> REDSTONE_CORE = registerSkullBlockItem("redstone_core", REDSTONE_CORE_TEXTURE);
    public static DeferredHolder<Block, PlayerHeadBlock> REDSTONE_CONTROLLER = registerSkullBlockItem("redstone_controller", REDSTONE_CONTROLLER_TEXTURE);
    public static DeferredHolder<Block, Formatter> FORMATTER_BLOCK = registerBlockItem(FORMATTER_ID, () -> new Formatter(BlockBehaviour.Properties.of().strength(defaultDestroyTime, defaultStrength).sound(defaultComponentSound)));
    public static DeferredHolder<Block, Assembler> ASSEMBLER_BLOCK = registerBlockItem(ASSEMBLER_ID, () -> new Assembler(BlockBehaviour.Properties.of().strength(defaultDestroyTime, defaultStrength).sound(defaultComponentSound)));
    public static DeferredHolder<Block, Dumper> DUMPER_BLOCK = registerBlockItem(DUMPER_ID, () -> new Dumper(BlockBehaviour.Properties.of().strength(defaultDestroyTime, defaultStrength).sound(defaultComponentSound)));
    public static DeferredHolder<Block, Disassembler> DISASSEMBLER_BLOCK = registerBlockItem(DISASSEMBLER_ID, () -> new Disassembler(BlockBehaviour.Properties.of().strength(defaultDestroyTime, defaultStrength).sound(defaultComponentSound)));
    public static final DeferredHolder<Block, MicrocontrollerBlock> MICROCONTROLLER_BLOCK = registerBlockItem(MICROCONTROLLER_ID,
            () -> new MicrocontrollerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).strength(0.0F).sound(SoundType.WOOD)));

    public static final DeferredHolder<Block, Programmer> PROGRAMMER = registerBlockItem(PROGRAMMER_ID,
            () -> new Programmer(BlockBehaviour.Properties.of().strength(2.0F, 3.0F).mapColor(MapColor.WOOD)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MicrocontrollerBlockEntity>> MICROCONTROLLER_BE =
            BLOCK_ENTITIES.register(MICROCONTROLLER_ID, () -> BlockEntityType.Builder.of(MicrocontrollerBlockEntity::new, MICROCONTROLLER_BLOCK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProgrammerBlockEntity>> PROGRAMMER_BE =
            BLOCK_ENTITIES.register(PROGRAMMER_ID, () -> BlockEntityType.Builder.of(ProgrammerBlockEntity::new, PROGRAMMER.get()).build(null));

    public static void register(IEventBus bus) {
        bus.addListener(EventPriority.HIGH, MinecontrollersBlocks::HighPriorityCommonSetup);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
    public static void registerSkullBlocks(){
        for(Field validBlocksField : BlockEntityType.SKULL.getClass().getDeclaredFields()){
            boolean acces = validBlocksField.isAccessible();
            boolean wasRegistered = false;
            try{
                validBlocksField.setAccessible(true);
                if(validBlocksField.get(BlockEntityType.SKULL) instanceof Set<?> validBlocks && validBlocks.contains(Blocks.PLAYER_HEAD)){
                    HashSet<Block> newValidBlocks = new HashSet<>();
                    newValidBlocks.addAll((Set<Block>)validBlocks);
                    for(DeferredHolder<Block, ? extends Block> skull : skullBlocks)
                        newValidBlocks.add(skull.get());
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
    public static void HighPriorityCommonSetup(FMLCommonSetupEvent event){
        MicrocontrollerBlock microcontrollerBlock = MICROCONTROLLER_BLOCK.get();
        Programmer programmer = PROGRAMMER.get();
        MoreRedAPI.getCableConnectabilityRegistry().put(microcontrollerBlock, (WireConnector)(world, thisPos, thisState, wirePos, wireState, wireFace, directionToWire) -> microcontrollerBlock.canConnectToAdjacentCable(world, thisPos, thisState, wirePos, wireState, wireFace, directionToWire));
        MoreRedAPI.getCableConnectabilityRegistry().put(programmer, (WireConnector)(world, thisPos, thisState, wirePos, wireState, wireFace, directionToWire) -> programmer.canConnectToAdjacentCable(world, thisPos, thisState, wirePos, wireState, wireFace, directionToWire));
    }
}