package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;

public final class AutoRespawnFeature {
    private static long deathScreenOpenedAtMs = -1L;

    private AutoRespawnFeature() {}

    public static void tick(Minecraft client) {
        if (!Feature.AUTO_RESPAWN.isEnabled()) {
            deathScreenOpenedAtMs = -1L;
            return;
        }
        LocalPlayer player = client.player;
        if (player == null) {
            deathScreenOpenedAtMs = -1L;
            return;
        }
        if (!(client.screen instanceof DeathScreen)) {
            deathScreenOpenedAtMs = -1L;
            return;
        }
        if (client.level != null && client.level.getLevelData().isHardcore()) return;

        long now = System.currentTimeMillis();
        if (deathScreenOpenedAtMs < 0) {
            deathScreenOpenedAtMs = now;
            return;
        }
        int delay = Math.max(0, IntrinsicClient.getConfig().autoRespawnDelayMs);
        if (now - deathScreenOpenedAtMs < delay) return;

        player.respawn();
        client.setScreen(null);
        deathScreenOpenedAtMs = -1L;
    }
}
