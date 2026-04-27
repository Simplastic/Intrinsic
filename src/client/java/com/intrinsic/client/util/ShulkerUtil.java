package com.intrinsic.client.util;

import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public final class ShulkerUtil {
    private ShulkerUtil() {}

    public static boolean isShulkerBox(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock;
    }

    public static List<ItemStack> getContents(ItemStack stack) {
        if (!isShulkerBox(stack)) return Collections.emptyList();
        ItemContainerContents c = stack.get(DataComponents.CONTAINER);
        if (c == null) return Collections.emptyList();
        return c.allItemsCopyStream().toList();
    }

    public static int countContents(ItemStack stack) {
        if (!isShulkerBox(stack)) return 0;
        ItemContainerContents c = stack.get(DataComponents.CONTAINER);
        if (c == null) return 0;
        return c.nonEmptyItemCopyStream().mapToInt(ItemStack::getCount).sum();
    }
}
