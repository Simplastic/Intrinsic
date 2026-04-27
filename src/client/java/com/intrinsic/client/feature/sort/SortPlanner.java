package com.intrinsic.client.feature.sort;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SortPlanner {
    private SortPlanner() {}

    public static List<ClickOp> plan(AbstractContainerScreen<?> screen, SortTarget target) {
        AbstractContainerMenu handler = screen.getMenu();
        List<Integer> ids = target.slotIds();
        int n = ids.size();

        ItemStack[] stacks = new ItemStack[n];
        for (int i = 0; i < n; i++) {
            stacks[i] = handler.slots.get(ids.get(i)).getItem().copy();
        }

        List<ClickOp> ops = new ArrayList<>();
        mergePartials(stacks, ids, ops);

        ItemStack[] desired = new ItemStack[n];
        for (int i = 0; i < n; i++) desired[i] = stacks[i].copy();
        Arrays.sort(desired, STACK_COMPARATOR);

        for (int i = 0; i < n; i++) {
            if (stackEqualsExact(stacks[i], desired[i])) continue;
            int j = findExactMatch(stacks, i + 1, desired[i]);
            if (j < 0) continue;
            // Universal 3-click swap: click(j); click(i); click(j).
            // Works for different-item swaps, empty i, and same-key cases (partial merges into i;
            // remainder is then placed back into j, yielding the intended (i, j) exchange).
            ops.add(new ClickOp(ids.get(j)));
            ops.add(new ClickOp(ids.get(i)));
            ops.add(new ClickOp(ids.get(j)));
            ItemStack tmp = stacks[i];
            stacks[i] = stacks[j];
            stacks[j] = tmp;
        }

        return ops;
    }

    private static void mergePartials(ItemStack[] stacks, List<Integer> ids, List<ClickOp> ops) {
        int n = stacks.length;
        Map<StackKey, List<Integer>> groups = new HashMap<>();
        for (int i = 0; i < n; i++) {
            if (stacks[i].isEmpty()) continue;
            groups.computeIfAbsent(new StackKey(stacks[i]), k -> new ArrayList<>()).add(i);
        }
        for (List<Integer> group : groups.values()) {
            if (group.size() < 2) continue;
            while (true) {
                List<Integer> nonFull = new ArrayList<>();
                for (int idx : group) {
                    if (!stacks[idx].isEmpty()
                            && stacks[idx].getCount() < stacks[idx].getItem().getDefaultMaxStackSize()) {
                        nonFull.add(idx);
                    }
                }
                if (nonFull.size() < 2) break;
                nonFull.sort((a, b) -> Integer.compare(stacks[b].getCount(), stacks[a].getCount()));
                int dst = nonFull.get(0);
                int src = nonFull.get(nonFull.size() - 1);
                int space = stacks[dst].getItem().getDefaultMaxStackSize() - stacks[dst].getCount();
                int moved = Math.min(stacks[src].getCount(), space);
                if (moved <= 0) break;

                ops.add(new ClickOp(ids.get(src)));
                ops.add(new ClickOp(ids.get(dst)));
                stacks[dst].setCount(stacks[dst].getCount() + moved);
                int remaining = stacks[src].getCount() - moved;
                if (remaining == 0) {
                    stacks[src] = ItemStack.EMPTY;
                } else {
                    ops.add(new ClickOp(ids.get(src)));
                    stacks[src].setCount(remaining);
                }
            }
        }
    }

    private static final Comparator<ItemStack> STACK_COMPARATOR = Comparator
            .comparing((ItemStack s) -> s.isEmpty())
            .thenComparing(SortPlanner::idOf)
            .thenComparingInt(ItemStack::getDamageValue)
            .thenComparingInt(s -> -s.getCount());

    private static boolean stackEqualsExact(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) return true;
        if (a.isEmpty() != b.isEmpty()) return false;
        return a.getCount() == b.getCount() && ItemStack.isSameItemSameComponents(a, b);
    }

    private static int findExactMatch(ItemStack[] stacks, int from, ItemStack target) {
        for (int k = from; k < stacks.length; k++) {
            if (stackEqualsExact(stacks[k], target)) return k;
        }
        return -1;
    }

    private static String idOf(ItemStack s) {
        if (s.isEmpty()) return "";
        Identifier id = BuiltInRegistries.ITEM.getKey(s.getItem());
        return id == null ? "" : id.toString();
    }
}
