package com.abadon.minecontrollers.entityblocks.programmer;

import com.abadon.minecontrollers.MinecontrollersBlocks;
import com.abadon.minecontrollers.inventory.ProgrammerMenu;
import commoble.morered.api.ChanneledPowerSupplier;
import commoble.morered.plate_blocks.PlateBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup.Provider;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ProgrammerBlockEntity extends BaseContainerBlockEntity implements ChanneledPowerSupplier {
    protected int page = 0;
    protected int line = 0;
    protected byte power[] = new byte[16];
    protected Lazy<ChanneledPowerSupplier> powerHolder = Lazy.of(() -> this);
    protected ItemStack item = ItemStack.EMPTY;
    protected boolean exitFlag = false;
    public ProgrammerBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }
    public boolean getWorkStatus(){
        return exitFlag;
    }
    public void invokeSeeker(Level level, BlockState blockState, BlockPos blockPos){
        //Logger logger = LogUtils.getLogger();
        //logger.info("seeker was invoked");
        var bookData = item.get(DataComponents.WRITABLE_BOOK_CONTENT);
        if(!item.getComponents().isEmpty() && bookData != null){
            List<Filterable<String>> pages = item.get(DataComponents.WRITABLE_BOOK_CONTENT).pages();
            if(page >= pages.size()) page = 0;
            String lines[] = pages.get(page).raw().split("\n");
            if(line >= lines.length) {
                page++;
                line = 0;
            }
            if(page >= pages.size()){
                page = 0;
                exitFlag = true;
                return;
            }
            else{
                exitFlag = false;
            }
            //logger.info("serializing line - " + pagesArr[page].getAsString().split("\n")[line]);
            char[] data = pages.get(page).raw().split("\n")[line].toCharArray();
            if(data.length <= 16){
                for(int i = 0; i < 16; i++){
                    if(data.length > i){
                        char chanelData = data[i];
                        //logger.info("serializing char - " + chanelData);
                        try{
                            power[i] = (byte)Integer.parseInt(String.valueOf(chanelData), 32);
                        } catch (NumberFormatException exception){
                            power[i] = 0;
                        }
                    }
                    else power[i] = 0;
                }
            }
            line++;
            this.setChanged();
        }
        else{
            page = 0;
            line = 0;
        }
    }
    public ProgrammerBlockEntity(BlockPos pos, BlockState state) {
        this(MinecontrollersBlocks.PROGRAMMER_BE.get(), pos, state);
    }
    @Override
    public void saveAdditional(CompoundTag compound, Provider registries) {
        super.saveAdditional(compound, registries);
        compound.putInt("page", page);
        compound.putByteArray("power", power);
        compound.remove("textbook");
        compound.put("textbook", item.save(level.registryAccess()));
        compound.putInt("line", line);
        compound.putBoolean("exitFlag", exitFlag);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.minecontrollers.bundled_programmer");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return NonNullList.withSize(1, item);
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        item = nonNullList.get(0);
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        if(canOpen(inventory.player))
            return new ProgrammerMenu(i, inventory, this);
        else return null;
    }
    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        CustomData data = input.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null) {
            data.loadInto(this, this.level.registryAccess());
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        CompoundTag compound = new CompoundTag();
        compound.putInt("page", page);
        compound.putByteArray("power", power);
        compound.remove("textbook");
        compound.put("textbook", item.save(level.registryAccess()));
        compound.putInt("line", line);
        compound.putBoolean("exitFlag", exitFlag);
        this.saveAdditional(compound, this.level.registryAccess());
        builder.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(compound));
    }
    @Override
    public void loadAdditional(CompoundTag compound, Provider registries) {
        super.loadAdditional(compound, registries);
        page = compound.getInt("page");
        power = compound.getByteArray("power").clone();
        line = compound.getInt("line");
        if(compound.contains("textbook", Tag.TAG_COMPOUND)){
            item = ItemStack.parse(registries, compound.getCompound("textbook")).orElse(ItemStack.EMPTY);
        }
        exitFlag = compound.getBoolean("exitFlag");
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return item.equals(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItem(int i) {
        return item;
    }

    @Override
    public ItemStack removeItem(int i, int i1) {
        ItemStack ret = item;
        item = ItemStack.EMPTY;
        if (!item.isEmpty()) {
            BlockState blockState = getBlockState();
            RedStoneWireBlock.updateOrDestroy(blockState, blockState.setValue(Programmer.HAS_BOOK, false), getLevel(), getBlockPos(), Block.UPDATE_ALL);
            this.setChanged();
        }

        return ret;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        if(itemStack.getItem().getDefaultInstance().getItem().equals(Items.WRITABLE_BOOK) ||
                itemStack.getItem().getDefaultInstance().getItem().equals(Items.WRITTEN_BOOK)){
            item = itemStack;
            BlockState blockState = getBlockState();
            RedStoneWireBlock.updateOrDestroy(blockState, blockState.setValue(Programmer.HAS_BOOK, true), getLevel(), getBlockPos(), Block.UPDATE_ALL);
        }
        else{
            BlockState blockState = getBlockState();
            RedStoneWireBlock.updateOrDestroy(blockState, blockState.setValue(Programmer.HAS_BOOK, false), getLevel(), getBlockPos(), Block.UPDATE_ALL);
            item = ItemStack.EMPTY;
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {

    }

    @Override
    public int getPowerOnChannel(@NotNull Level world, @NotNull BlockPos wirePos, @NotNull BlockState wireState, @Nullable Direction wireFace, int channel) {
        if(getBlockState().getBlock() instanceof Programmer programmer){
            if(getBlockPos().relative(programmer.getLookDirection(getBlockState())).equals(wirePos)){
                return power[channel];
            }
        }
        return 0;
    }

    public @Nullable ChanneledPowerSupplier getChanneledPower(@NotNull Direction side) {
        return side == PlateBlockStateProperties.getOutputDirection(this.getBlockState())
                ? this::getPowerOnChannel
                : null;
    }
}
