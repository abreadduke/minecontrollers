package com.abadon.minecontrollers.inventory;

import com.abadon.minecontrollers.blocks.disassembler.CodeDisassembler;
import com.abadon.minecontrollers.utils.BookPagesReader;
import com.abadon.minecontrollers.utils.BookPagesWriter;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DisassemblerMenu extends TranslatorBlockMenu{
    protected DisassemblerMenu(@Nullable MenuType<?> p_38851_, int p_38852_) {
        super(p_38851_, p_38852_);
    }
    public DisassemblerMenu(int value, Inventory inventory){
        this(value, inventory, ContainerLevelAccess.NULL);
    }
    public DisassemblerMenu(int value, Inventory inventory, ContainerLevelAccess levelAccess){
        this(value, inventory, levelAccess, ModMenu.ASSEMBLER_MENU_MENU_TYPE.get());
    }
    public DisassemblerMenu(int value, Inventory inventory, ContainerLevelAccess levelAccess, MenuType<?> menuType){
        super(value, inventory, levelAccess, menuType);
    }

    @Override
    protected void translateBook(Container container) {
        if(container.getItem(0) == ItemStack.EMPTY || container.getItem(1) != ItemStack.EMPTY) return;
        CodeDisassembler codeAssembler = new CodeDisassembler();
        ItemStack book = container.getItem(0);
        ItemStack newBook = book.copy();
        try{
            String asmCode = codeAssembler.disassembly(new BookPagesReader().readBook(book));
            new BookPagesWriter().writeBook(newBook, asmCode);
            container.setItem(0, ItemStack.EMPTY);
            container.setItem(1, newBook);
        } catch (Exception ex){
            container.setItem(1, container.getItem(0));
            container.setItem(0, ItemStack.EMPTY);
        }
    }
}
