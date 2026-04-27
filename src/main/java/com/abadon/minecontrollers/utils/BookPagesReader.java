package com.abadon.minecontrollers.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BookPagesReader implements IBookReader {

    @Override
    public String readBook(ItemStack book) {
        List<Filterable<String>> pages = book.get(DataComponents.WRITABLE_BOOK_CONTENT).pages();
        StringBuilder textBuilder = new StringBuilder();

        for (net.minecraft.server.network.Filterable<String> page : pages) {
            textBuilder.append("\n").append(page.raw());
        }

        return textBuilder.toString();
    }
}
