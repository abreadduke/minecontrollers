package com.abadon.minecontrollers.inventory;

import com.abadon.minecontrollers.blocks.formatter.BookFormatter;
import com.abadon.minecontrollers.utils.BookMenuUtils;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class TranslatorBlockMenu extends AbstractContainerMenu {
    protected Inventory inventory;
    protected CraftingContainer container;
    protected ContainerLevelAccess levelAccess;
    protected Slot inputSlot;
    protected Slot outputSlot;
    private boolean lockchangingsFlag = false;
    protected TranslatorBlockMenu(@Nullable MenuType<?> p_38851_, int p_38852_) {
        super(p_38851_, p_38852_);
    }
    @Override
    public void removed(Player player) {
        super.removed(player);
        ItemStack inputItem = slots.get(0).getItem();
        ItemStack outputItem = slots.get(1).getItem();
        if (player.isAlive()) {
            if(inputItem != ItemStack.EMPTY)
                player.getInventory().placeItemBackInInventory(inputItem);
            if(outputItem != ItemStack.EMPTY)
                player.getInventory().placeItemBackInInventory(outputItem);
        } else {
            if(inputItem != ItemStack.EMPTY)
                player.drop(inputItem, false);
            if(outputItem != ItemStack.EMPTY)
                player.drop(outputItem, false);
        }
    }
    protected abstract void translateBook(Container container);
    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if(lockchangingsFlag) return;
        if(container.equals(this.container)){
            if(BookMenuUtils.canPlaceBookIntoContainer(container.getItem(0))){
                lockchangingsFlag = true;
                translateBook(container);
                lockchangingsFlag = false;
            }
        }
    }
    @Override
    public ItemStack quickMoveStack(Player player, int id) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(id);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (id < this.container.getContainerSize()) {
                if (!this.moveItemStackTo(itemstack1, this.container.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.container.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    public TranslatorBlockMenu(int value, Inventory inventory, ContainerLevelAccess levelAccess, MenuType<?> menuType){
        this(menuType, value);
        this.inventory = inventory;
        this.levelAccess = levelAccess;
        container = new TransientCraftingContainer(this, 2, 1);
        this.inputSlot = new Slot(container, 0, 44, 35){
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return BookMenuUtils.canPlaceBookIntoContainer(itemStack);
            }
        };
        this.outputSlot = new Slot(container, 1, 116, 35){
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }
        };
        addSlot(inputSlot);
        addSlot(outputSlot);
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
        }
    }
}
