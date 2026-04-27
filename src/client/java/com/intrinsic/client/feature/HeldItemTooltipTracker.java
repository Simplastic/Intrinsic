package com.intrinsic.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class HeldItemTooltipTracker {
    private static final long DISPLAY_MS = 2000L;
    private static final long FADE_MS = 400L;

    private static int lastSlot = -1;
    private static Component lastLabel = null;
    private static long shownAtMs = 0L;

    private HeldItemTooltipTracker() {}

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            lastSlot = -1;
            lastLabel = null;
            return;
        }
        int slot = player.getInventory().getSelectedSlot();
        if (slot != lastSlot) {
            ItemStack stack = player.getInventory().getSelectedItem();
            lastSlot = slot;
            if (stack == null || stack.isEmpty()) {
                lastLabel = null;
            } else {
                lastLabel = stack.getHoverName();
                shownAtMs = System.currentTimeMillis();
            }
        }
    }

    public static Component currentLabel() {
        return lastLabel;
    }

    public static float alpha() {
        if (lastLabel == null) return 0f;
        long age = System.currentTimeMillis() - shownAtMs;
        if (age < 0 || age > DISPLAY_MS) return 0f;
        long remain = DISPLAY_MS - age;
        if (remain >= FADE_MS) return 1f;
        return Math.max(0f, remain / (float) FADE_MS);
    }
}
