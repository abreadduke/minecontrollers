package com.abadon.minecontrollers.blocks.formatter;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;

public class BookFormatter {
    private final String OFFSET_PARAMETER = "^OFFSET:.*";
    private final String GLOBAL_OFFSET_PARAMETER = "^GLOFFSET:.*";
    //TODO: point in the documentation that minecraft vanilla book can contain only 100 pages +documentation
    public ItemStack format(ItemStack book){
        ItemStack newbook = book.copy();
        ListTag pages = book.getTag().getList("pages", 8);
        StringTag pagesArr[] = new StringTag[pages.size()];
        pages.toArray(pagesArr);
        ListTag formattedPages = new ListTag();
        int address = 0;
        StringBuilder formattedPageBuilder = new StringBuilder();
        for(StringTag page : pagesArr){
            String lines[] = page.getAsString().split("\n");
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
                    formattedPages.add(StringTag.valueOf(formattedPageBuilder.toString()));
                    formattedPageBuilder = new StringBuilder();
                }
                //}
            }
        }
        newbook.getTag().remove("pages");
        newbook.addTagElement("pages", formattedPages);
        return newbook;
    }
}
