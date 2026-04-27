package com.intrinsic.client.feature.sort;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public final class SortTargetResolver {
    private SortTargetResolver() {}

    public static SortTarget resolve(AbstractContainerScreen<?> screen, Slot hovered) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;
        Inventory playerInv = mc.player.getInventory();
        AbstractContainerMenu handler = screen.getMenu();

        boolean playerMain;
        Container target;
        String label;

        Container inv = (hovered != null) ? hovered.container : null;
        boolean hoveredIsContainer = inv != null
                && inv != playerInv
                && inv.getContainerSize() >= 9
                && !isSpecialContainer(inv);

        if (hoveredIsContainer) {
            target = inv;
            label = "Container";
            playerMain = false;
        } else {
            target = playerInv;
            label = "Inventory";
            playerMain = true;
        }

        List<Integer> ids = new ArrayList<>();
        for (int menuIdx = 0; menuIdx < handler.slots.size(); menuIdx++) {
            Slot s = handler.slots.get(menuIdx);
            if (playerMain) {
                if (s.container == playerInv) {
                    int containerIdx = s.index;
                    if (containerIdx >= 9 && containerIdx < 36) ids.add(menuIdx);
                }
            } else {
                if (s.container == target) ids.add(menuIdx);
            }
        }
        if (ids.size() < 2) return null;
        return new SortTarget(ids, label);
    }

    private static boolean isSpecialContainer(Container inv) {
        String name = inv.getClass().getName();
        return name.contains("Crafting")
            || name.contains("Furnace")
            || name.contains("Smoker")
            || name.contains("BrewingStand")
            || name.contains("Anvil")
            || name.contains("Smithing")
            || name.contains("Grindstone")
            || name.contains("Loom")
            || name.contains("Stonecutter")
            || name.contains("Beacon")
            || name.contains("Enchantment")
            || name.contains("Merchant")
            || name.contains("Horse");
    }
}
