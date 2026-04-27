package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import com.intrinsic.client.mixin.MinecraftClientAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

public final class AutoEatFeature {
    private static int previousSelectedSlot = -1;
    private static boolean holdingUse = false;

    private AutoEatFeature() {}

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (!Feature.AUTO_EAT.isEnabled() || player == null || client.screen != null) {
            stopHoldingAndRestore(client, player);
            return;
        }
        if (!player.canEat(false)) {
            stopHoldingAndRestore(client, player);
            return;
        }

        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        int threshold = Math.max(0, Math.min(19, cfg.autoEatThreshold));
        if (player.getFoodData().getFoodLevel() > threshold) {
            stopHoldingAndRestore(client, player);
            return;
        }

        Inventory inv = player.getInventory();
        ItemStack offhand = player.getOffhandItem();
        FoodProperties offFood = offhand.get(DataComponents.FOOD);
        if (offFood != null) {
            holdUse(client, InteractionHand.OFF_HAND);
            return;
        }

        int bestSlot = -1;
        int bestNutrition = 0;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = inv.getItem(slot);
            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food == null) continue;
            if (food.nutrition() > bestNutrition) {
                bestNutrition = food.nutrition();
                bestSlot = slot;
            }
        }
        if (bestSlot < 0) {
            stopHoldingAndRestore(client, player);
            return;
        }

        if (inv.getSelectedSlot() != bestSlot) {
            if (previousSelectedSlot < 0) previousSelectedSlot = inv.getSelectedSlot();
            inv.setSelectedSlot(bestSlot);
        }
        holdUse(client, InteractionHand.MAIN_HAND);
    }

    private static void holdUse(Minecraft client, InteractionHand hand) {
        if (!holdingUse) {
            client.options.keyUse.setDown(true);
            holdingUse = true;
        }
        ((MinecraftClientAccessor) client).setItemUseCooldown(0);
    }

    private static void stopHoldingAndRestore(Minecraft client, LocalPlayer player) {
        if (holdingUse) {
            client.options.keyUse.setDown(false);
            holdingUse = false;
        }
        if (player != null) restoreSlot(player);
    }

    private static void restoreSlot(LocalPlayer player) {
        if (previousSelectedSlot < 0) return;
        Inventory inv = player.getInventory();
        if (previousSelectedSlot < 9 && inv.getSelectedSlot() != previousSelectedSlot) {
            inv.setSelectedSlot(previousSelectedSlot);
        }
        previousSelectedSlot = -1;
    }
}
