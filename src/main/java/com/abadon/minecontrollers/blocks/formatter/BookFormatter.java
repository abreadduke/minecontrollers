package com.abadon.minecontrollers.blocks.formatter;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;

import java.util.ArrayList;
import java.util.List;

public class BookFormatter {
    private final String OFFSET_PARAMETER = "^OFFSET:.*";
    private final String GLOBAL_OFFSET_PARAMETER = "^GLOFFSET:.*";
    public ItemStack format(ItemStack book){
        ItemStack newbook = book.copy();
        List<Filterable<String>> pages = book.get(DataComponents.WRITABLE_BOOK_CONTENT).pages();
        List<Filterable<String>> formattedPages = new ArrayList<>();
        int address = 0;
        StringBuilder formattedPageBuilder = new StringBuilder();
        for(Filterable<String> page : pages){
            String lines[] = page.raw().split("\n");
            int compiledLines = 0;
            for(int l = 0; l < lines.length; l++){
                if(lines[l].matches(OFFSET_PARAMETER)){
                    address += Integer.valueOf(lines[l].split(":\\s*")[1], 16);
                    continue;
                } else if (lines[l].matches(GLOBAL_OFFSET_PARAMETER)) {
                    address = Integer.valueOf(lines[l].split(":\\s*")[1], 16);
                    continue;
                }
                //if(lines[l].length() == 12){
                int addressMask = 15;
                char chars[] = lines[l].toCharArray();
                for(int i = 0; i < chars.length; i += 2){
                    String memoryAddressPattern = new StringBuilder().
                            append(Integer.toString(address >>> 12, 16)).
                            append(Integer.toString((address >>> 8) & addressMask, 16)).
                            append(Integer.toString((address >>> 4) & addressMask, 16)).
                            append(Integer.toString(address & addressMask, 16)).toString().toUpperCase();
                    String value = new StringBuilder().append(chars[i]).append(chars[i+1]).toString().toUpperCase();
                    String blockOutput = memoryAddressPattern + value + "F00F\n"; //builds new programming instrucion line
                    formattedPageBuilder.append(blockOutput);
                    address++;
                    compiledLines++;
                }
                if(compiledLines % 12 == 0 || l == lines.length - 1) {
                    compiledLines = 0;
                    formattedPages.add(Filterable.passThrough(formattedPageBuilder.toString()));
                    formattedPageBuilder = new StringBuilder();
                }
                //}
            }
        }
        WritableBookContent content = new WritableBookContent(formattedPages);
        newbook.set(DataComponents.WRITABLE_BOOK_CONTENT, content);
        return newbook;
    }
}
