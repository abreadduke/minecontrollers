package com.abadon.minecontrollers.inventory;

import com.abadon.minecontrollers.blocks.assembler.CodeAssembler;
import com.abadon.minecontrollers.utils.BookPagesReader;
import com.abadon.minecontrollers.utils.BookPagesWriter;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AssemblerMenu extends TranslatorBlockMenu{
    protected AssemblerMenu(@Nullable MenuType<?> p_38851_, int p_38852_) {
        super(p_38851_, p_38852_);
    }
    public AssemblerMenu(int value, Inventory inventory){
        this(value, inventory, ContainerLevelAccess.NULL);
    }
    public AssemblerMenu(int value, Inventory inventory, ContainerLevelAccess levelAccess){
        this(value, inventory, levelAccess, ModMenu.ASSEMBLER_MENU_MENU_TYPE.get());
    }
    public AssemblerMenu(int value, Inventory inventory, ContainerLevelAccess levelAccess, MenuType<?> menuType){
        super(value, inventory, levelAccess, menuType);
    }
    @Override
    protected void translateBook(Container container) {
        if(container.getItem(0) == ItemStack.EMPTY) return;
        CodeAssembler codeAssembler = new CodeAssembler();
        ItemStack book = container.getItem(0);
        ItemStack newBook = book.copy();
        try{
            String mashineCodes = codeAssembler.assembly(new BookPagesReader().readBook(book));
            new BookPagesWriter().writeBook(newBook, mashineCodes);
            container.setItem(0, ItemStack.EMPTY);
            container.setItem(1, newBook);
        } catch (Exception ex){
            container.setItem(1, container.getItem(0));
            container.setItem(0, ItemStack.EMPTY);
        }

    }
}
