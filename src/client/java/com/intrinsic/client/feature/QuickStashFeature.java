package com.intrinsic.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class QuickStashFeature {
    private QuickStashFeature() {}

    // KeyMapping.consumeClick() does not fire while a Screen is open — vanilla
    // routes keypresses to Screen.keyPressed instead. HandledScreenKeyMixin
    // calls this method directly from its keyPressed injection.
    public static boolean trigger(AbstractContainerScreen<?> screen, Slot hovered) {
        if (!Feature.QUICK_STASH.isEnabled()) return false;
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.gameMode == null) return false;
        if (screen instanceof CreativeModeInventoryScreen) return false;
        AbstractContainerMenu menu = screen.getMenu();
        if (menu instanceof InventoryMenu) return false;
        if (hovered == null) return false;

        ItemStack hoveredStack = hovered.getItem();
        if (hoveredStack.isEmpty()) return false;
        Item target = hoveredStack.getItem();

        Inventory inv = player.getInventory();
        // Direction follows the hovered slot: hover a player-inventory slot to
        // deposit matching stacks into the container, hover a container slot
        // to pull matching stacks out into the player inventory.
        boolean fromPlayer = hovered.container == inv;
        boolean moved = false;
        for (Slot s : menu.slots) {
            boolean playerSlot = s.container == inv;
            if (fromPlayer ? !playerSlot : playerSlot) continue;
            ItemStack stack = s.getItem();
            if (stack.isEmpty() || stack.getItem() != target) continue;
            client.gameMode.handleContainerInput(menu.containerId, s.index, 0,
                    ContainerInput.QUICK_MOVE, player);
            moved = true;
        }
        return moved;
    }
}
