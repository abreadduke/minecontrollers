package com.abadon.minecontrollers.utils;

import net.minecraft.world.item.ItemStack;

public interface IBookPagesWriter {
    void writeBook(ItemStack book, String info);
}
