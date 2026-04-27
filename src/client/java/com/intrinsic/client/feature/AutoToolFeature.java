package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class AutoToolFeature {
    private static int previousSelectedSlot = -1;

    private AutoToolFeature() {}

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (!Feature.AUTO_TOOL.isEnabled() || player == null || client.level == null) {
            restore(player);
            return;
        }
        if (client.screen != null) {
            restore(player);
            return;
        }
        if (!client.options.keyAttack.isDown()) {
            restore(player);
            return;
        }
        HitResult hr = client.hitResult;
        if (!(hr instanceof BlockHitResult bhr) || bhr.getType() == HitResult.Type.MISS) {
            restore(player);
            return;
        }

        BlockPos pos = bhr.getBlockPos();
        BlockState state = client.level.getBlockState(pos);
        if (state.isAir()) {
            restore(player);
            return;
        }

        Inventory inv = player.getInventory();
        int currentSlot = inv.getSelectedSlot();
        ItemStack currentStack = inv.getItem(currentSlot);
        float currentSpeed = effectiveSpeed(currentStack, state);

        int bestSlot = currentSlot;
        float bestSpeed = currentSpeed;
        for (int slot = 0; slot < 9; slot++) {
            if (slot == currentSlot) continue;
            ItemStack s = inv.getItem(slot);
            if (s.isEmpty()) continue;
            float speed = effectiveSpeed(s, state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = slot;
            }
        }

        if (bestSlot != currentSlot) {
            if (previousSelectedSlot < 0 && IntrinsicClient.getConfig().autoToolRestorePrevious) {
                previousSelectedSlot = currentSlot;
            }
            inv.setSelectedSlot(bestSlot);
        }
    }

    private static float effectiveSpeed(ItemStack stack, BlockState state) {
        if (stack == null || stack.isEmpty()) return 1.0f;
        float speed = stack.getDestroySpeed(state);
        if (stack.isCorrectToolForDrops(state)) speed *= 4.0f;
        return speed;
    }

    private static void restore(LocalPlayer player) {
        if (player == null) {
            previousSelectedSlot = -1;
            return;
        }
        if (previousSelectedSlot < 0) return;
        if (!IntrinsicClient.getConfig().autoToolRestorePrevious) {
            previousSelectedSlot = -1;
            return;
        }
        Inventory inv = player.getInventory();
        if (previousSelectedSlot < 9 && inv.getSelectedSlot() != previousSelectedSlot) {
            inv.setSelectedSlot(previousSelectedSlot);
        }
        previousSelectedSlot = -1;
    }
}
