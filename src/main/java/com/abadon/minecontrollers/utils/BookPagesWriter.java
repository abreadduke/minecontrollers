package com.abadon.minecontrollers.utils;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;

public class BookPagesWriter implements IBookPagesWriter{
    @Override
    public void writeBook(ItemStack book, String info) {
        ListTag pages = new ListTag();
        String lines[] = info.split("\n");
        StringBuilder pageBuilder = new StringBuilder();
        for(int i = 0; i < lines.length; i++){
            pageBuilder.append(lines[i]).append("\n");
            if((i + 1) % 15 == 0 || i == lines.length - 1){
                pages.add(StringTag.valueOf(pageBuilder.toString()));
                pageBuilder = new StringBuilder();
            }
        }
        if(book.getTag() != null)
            book.getTag().remove("pages");
        book.addTagElement("pages", pages);
    }
}
