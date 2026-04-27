package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.Minecraft;

public class SprintToggleFeature {
    public static void tick(Minecraft client) {
        if (!IntrinsicClient.getConfig().autoSprint) return;
        if (client.player == null) return;

        // Don't force sprint in certain situations
        if (client.player.isShiftKeyDown()) return;
        if (client.player.isUnderWater()) return;
        if (client.player.isFallFlying()) return;
        if (client.player.getFoodData().getFoodLevel() <= 6) return;

        // Sprint if moving forward
        if (client.player.input.hasForwardImpulse()) {
            client.player.setSprinting(true);
        }
    }
}
