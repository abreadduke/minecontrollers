package com.abadon.minecontrollers.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;

import java.util.ArrayList;
import java.util.List;

public class BookPagesWriter implements IBookPagesWriter{
    @Override
    public void writeBook(ItemStack book, String info) {
        List<Filterable<String>> pages = new ArrayList<>();
        String lines[] = info.split("\n");
        StringBuilder pageBuilder = new StringBuilder();
        for(int i = 0; i < lines.length; i++){
            pageBuilder.append(lines[i]).append("\n");
            if((i + 1) % 15 == 0 || i == lines.length - 1){
                pages.add(Filterable.passThrough(pageBuilder.toString()));
                pageBuilder = new StringBuilder();
            }
        }
        WritableBookContent component = new WritableBookContent(pages);
        book.set(DataComponents.WRITABLE_BOOK_CONTENT, component);
    }
}
