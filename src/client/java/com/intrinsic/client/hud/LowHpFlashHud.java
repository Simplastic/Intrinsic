package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;

public final class LowHpFlashHud {
    private LowHpFlashHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        Player player = client.player;
        if (player == null || player.isSpectator() || player.isCreative()) return;

        float max = player.getMaxHealth();
        if (max <= 0f) return;
        float ratio = player.getHealth() / max;
        double threshold = Math.max(0.05, Math.min(1.0, cfg.lowHpFlashThreshold));
        if (ratio > threshold) return;

        // Severity: 0 at threshold, 1 when dead. Ramps alpha with how far below the threshold we are.
        double severity = Math.max(0.0, Math.min(1.0, (threshold - ratio) / threshold));
        double pulse = 0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 160.0);
        int baseAlpha = 40 + (int) (160 * severity);
        int alpha = Math.max(0, Math.min(255, (int) (baseAlpha * (0.55 + 0.45 * pulse))));
        int color = (alpha << 24) | 0x00FF3030;

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int band = Math.max(16, Math.min(sw, sh) / 6);

        ctx.fill(0, 0, sw, band, color);
        ctx.fill(0, sh - band, sw, sh, color);
        ctx.fill(0, band, band, sh - band, color);
        ctx.fill(sw - band, band, sw, sh - band, color);
    }
}
