package com.abadon.minecontrollers.inventory;

import com.abadon.minecontrollers.blocks.formatter.BookFormatter;
import com.abadon.minecontrollers.utils.BookMenuUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FormatterMenu extends TranslatorBlockMenu {
    protected FormatterMenu(@Nullable MenuType<?> p_38851_, int p_38852_) {
        super(p_38851_, p_38852_);
    }
    public FormatterMenu(int value, Inventory inventory){
        this(value, inventory, ContainerLevelAccess.NULL);
    }
    public FormatterMenu(int value, Inventory inventory, ContainerLevelAccess levelAccess){
        this(value, inventory, levelAccess, ModMenu.FORMATTER_MENU_MENU_TYPE.get());
    }
    public FormatterMenu(int value, Inventory inventory, ContainerLevelAccess levelAccess, MenuType<?> menuType){
        super(value, inventory, levelAccess, menuType);
    }

    @Override
    protected void translateBook(Container container) {
        if(container.getItem(0) == ItemStack.EMPTY || container.getItem(1) != ItemStack.EMPTY) return;
        try{
            ItemStack newBook = new BookFormatter().format(container.getItem(0));
            if(!newBook.equals(ItemStack.EMPTY)){
                container.setItem(0, ItemStack.EMPTY);
                container.setItem(1, newBook);
            }
        } catch (Exception ex){
            container.setItem(1, container.getItem(0));
            container.setItem(0, ItemStack.EMPTY);
        }

    }
}
