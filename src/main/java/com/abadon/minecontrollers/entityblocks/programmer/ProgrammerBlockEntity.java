package com.abadon.minecontrollers.entityblocks.programmer;

import com.abadon.minecontrollers.entityblocks.MinecontrollersBlocks;
import com.abadon.minecontrollers.inventory.ProgrammerMenu;
import commoble.morered.api.ChanneledPowerSupplier;
import commoble.morered.api.MoreRedAPI;
import commoble.morered.plate_blocks.PlateBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProgrammerBlockEntity extends BaseContainerBlockEntity implements ChanneledPowerSupplier {
    protected int page = 0;
    protected int line = 0;
    protected byte power[] = new byte[16];
    protected LazyOptional<ChanneledPowerSupplier> powerHolder = LazyOptional.of(() -> this);
    protected NonNullList<ItemStack> items;
    protected boolean exitFlag = false;
    public ProgrammerBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
        this.items = NonNullList.withSize(1, ItemStack.EMPTY);
    }
    public boolean getWorkStatus(){
        return exitFlag;
    }
    public void invokeSeeker(Level level, BlockState blockState, BlockPos blockPos){
        //Logger logger = LogUtils.getLogger();
        //logger.info("seeker was invoked");

        if(items.get(0).getTag() != null && items.get(0).getTag().contains("pages")){
            ListTag pages = items.get(0).getTag().getList("pages", 8);
            StringTag pagesArr[] = new StringTag[pages.size()];
            pages.toArray(pagesArr);
            String lines[] = pagesArr[page].getAsString().split("\n");
            if(line >= lines.length) {
                page++;
                line = 0;
            }
            if(page >= pagesArr.length){
                page = 0;
                exitFlag = true;
                return;
            }
            else{
                exitFlag = false;
            }
            //logger.info("serializing line - " + pagesArr[page].getAsString().split("\n")[line]);
            char[] data = pagesArr[page].getAsString().split("\n")[line].toCharArray();
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
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
    {
        if (cap == MoreRedAPI.CHANNELED_POWER_CAPABILITY)
            return side == PlateBlockStateProperties.getOutputDirection(this.getBlockState()) ? (LazyOptional<T>) this.powerHolder : LazyOptional.empty();
        return super.getCapability(cap, side);
    }
    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("page", page);
        compound.putByteArray("power", power);
        compound.remove("textbook");
        compound.put("textbook", items.get(0).serializeNBT());
        //CompoundTag textbookTag = compound.getCompound("textbook");
        //if(items.get(0).getTag() == null)
        //    textbookTag.put("tag", new CompoundTag());
        compound.putInt("line", line);
        compound.putBoolean("exitFlag", exitFlag);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.minecontrollers.bundled_programmer");
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        if(canOpen(inventory.player))
            return new ProgrammerMenu(i, inventory, this);
        else return null;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        page = compound.getInt("page");
        power = compound.getByteArray("power").clone();
        line = compound.getInt("line");
        if(compound.contains("textbook")){
            items.set(0, ItemStack.of(compound.getCompound("textbook")));
        }
        exitFlag = compound.getBoolean("exitFlag");
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).equals(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItem(int i) {
        return items.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int i1) {
        ItemStack itemstack = ContainerHelper.removeItem(items, i, i1);
        if (!itemstack.isEmpty()) {
            BlockState blockState = getBlockState();
            RedStoneWireBlock.updateOrDestroy(blockState, blockState.setValue(Programmer.HAS_BOOK, false), getLevel(), getBlockPos(), Block.UPDATE_ALL);
            this.setChanged();
        }

        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(items, i);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        if(itemStack.getItem().getDefaultInstance().getItem().equals(Items.WRITABLE_BOOK) ||
                itemStack.getItem().getDefaultInstance().getItem().equals(Items.WRITTEN_BOOK)){
            items.set(i, itemStack);
            BlockState blockState = getBlockState();
            RedStoneWireBlock.updateOrDestroy(blockState, blockState.setValue(Programmer.HAS_BOOK, true), getLevel(), getBlockPos(), Block.UPDATE_ALL);
        }
        else{
            BlockState blockState = getBlockState();
            RedStoneWireBlock.updateOrDestroy(blockState, blockState.setValue(Programmer.HAS_BOOK, false), getLevel(), getBlockPos(), Block.UPDATE_ALL);
            items.set(i, ItemStack.EMPTY);
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
}
