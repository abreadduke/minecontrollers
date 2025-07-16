package com.abadon.minecontrollers.utils;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BookMenuUtils {
    public static boolean canPlaceBookIntoContainer(ItemStack itemStack){
        return itemStack.getItem().getDefaultInstance().getItem().equals(Items.WRITABLE_BOOK) ||
                itemStack.getItem().getDefaultInstance().getItem().equals(Items.WRITTEN_BOOK);
    }
}
