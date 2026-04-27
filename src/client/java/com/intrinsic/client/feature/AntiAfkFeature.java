package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

public final class AntiAfkFeature {
    private AntiAfkFeature() {}

    private static int ticksSinceLastAction = 0;

    public static void tick(Minecraft client) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        if (!cfg.antiAfk) {
            ticksSinceLastAction = 0;
            return;
        }
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            ticksSinceLastAction = 0;
            return;
        }
        if (client.screen != null) return;

        int intervalTicks = Math.max(20, cfg.antiAfkIntervalSeconds) * 20;
        ticksSinceLastAction++;
        if (ticksSinceLastAction >= intervalTicks) {
            player.swing(InteractionHand.MAIN_HAND);
            ticksSinceLastAction = 0;
        }
    }
}
