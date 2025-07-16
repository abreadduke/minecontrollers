package com.abadon.minecontrollers.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import com.abadon.minecontrollers.utils.BookMenuUtils;

public class ProgrammerMenu extends AbstractContainerMenu {
    private int value;
    private Inventory inventory;
    private Container container;

    protected ProgrammerMenu(@Nullable MenuType<?> p_38851_, int p_38852_) {
        super(p_38851_, p_38852_);
    }
    public ProgrammerMenu(int value, Inventory inventory){
        this(value, inventory, new SimpleContainer(1));
    }
    public ProgrammerMenu(int value, Inventory inventory, Container container) {
        this(ModMenu.PROGRAMMER_MENU_MENU_TYPE.get(), value);
        this.value = value;
        this.inventory = inventory;
        this.container = container;
        container.startOpen(inventory.player);
        Slot bookSlot = new Slot(container, 0, 80, 35){
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return BookMenuUtils.canPlaceBookIntoContainer(itemStack);
            }
        };
        addSlot(bookSlot);
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
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
        return this.container.stillValid(player);
    }
}
