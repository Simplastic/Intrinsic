package com.intrinsic.client.feature.sort;

import net.minecraft.world.item.ItemStack;

final class StackKey {
    private final ItemStack rep;

    StackKey(ItemStack rep) {
        this.rep = rep;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StackKey other)) return false;
        return ItemStack.isSameItemSameComponents(this.rep, other.rep);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(rep.getItem());
    }
}
