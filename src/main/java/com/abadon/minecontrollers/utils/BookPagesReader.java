package com.abadon.minecontrollers.utils;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;

public class BookPagesReader implements IBookReader {

    @Override
    public String readBook(ItemStack book) {
        ListTag pages = book.getTag().getList("pages", 8);
        StringTag pagesArr[] = new StringTag[pages.size()];
        pages.toArray(pagesArr);
        StringBuilder textBuilder = new StringBuilder();
        for(StringTag page : pagesArr){
            textBuilder.append("\n").append(page.getAsString());
        }
        return textBuilder.toString();
    }
}
