package com.intrinsic.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;

public final class InsomniaTrackerHud {
    private static final int PHANTOM_THRESHOLD = 72000;

    private InsomniaTrackerHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        if (client.player == null || client.level == null) return;

        int since = client.player.getStats().getValue(Stats.CUSTOM, Stats.TIME_SINCE_REST);
        int remaining = Math.max(0, PHANTOM_THRESHOLD - since);
        long tod = client.level.getOverworldClockTime() % 24000L;
        boolean isNight = tod >= 13000L && tod <= 23000L;

        BlockPos head = client.player.blockPosition().above();
        boolean openSky = client.level.canSeeSky(head);

        String msg;
        int color;
        if (since >= PHANTOM_THRESHOLD && isNight && openSky) {
            msg = "PHANTOMS ACTIVE";
            color = 0xFFFF3333;
        } else if (since >= PHANTOM_THRESHOLD) {
            msg = "Ready - waiting for night/sky";
            color = 0xFFFFAA00;
        } else {
            float minutes = remaining / 1200.0f;
            float days = remaining / 24000.0f;
            msg = String.format("Phantoms in %.1f min (%.2f d)", minutes, days);
            color = 0xFF55FFFF;
        }

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int x = HudPositions.x("insomniaTracker", sw);
        int y = HudPositions.y("insomniaTracker", sh);

        int w = client.font.width(msg) + 12;
        HudStyle.card(ctx, x, y, w, 14);
        ctx.text(client.font, Component.literal(msg), x + 6, y + 3, color, true);
    }
}
