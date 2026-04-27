package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import com.intrinsic.client.feature.Feature;
import com.intrinsic.client.gui.widget.ThemeColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class WaypointListHud {
    private WaypointListHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        if (client.player == null || client.level == null) return;

        String dim = client.level.dimension().identifier().toString();
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        if (!cfg.showWaypointListHud) return;
        if (cfg.waypoints.isEmpty()) return;

        double px = client.player.getX();
        double pz = client.player.getZ();

        List<IntrinsicConfig.Waypoint> here = new ArrayList<>();
        for (IntrinsicConfig.Waypoint wp : cfg.waypoints) {
            if (wp.dimension != null && !wp.dimension.isEmpty() && !wp.dimension.equals(dim)) continue;
            here.add(wp);
        }
        if (here.isEmpty()) return;
        here.sort(Comparator.comparingDouble(w -> {
            double dx = w.x - px, dz = w.z - pz;
            return dx * dx + dz * dz;
        }));

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int x = HudPositions.x("waypointList", sw);
        int y = HudPositions.y("waypointList", sh);

        int rows = Math.min(5, here.size());
        int w = 140;
        int h = 14 + rows * 10;

        HudStyle.card(ctx, x, y, w, h);
        ctx.text(client.font, Component.literal("Waypoints"), x + 6, y + 3, ThemeColors.TEXT, true);

        int yy = y + 14;
        for (int i = 0; i < rows; i++) {
            IntrinsicConfig.Waypoint wp = here.get(i);
            double dx = wp.x - px, dz = wp.z - pz;
            int dist = (int) Math.sqrt(dx * dx + dz * dz);
            String line = wp.name + "  " + dist + "m";
            ctx.text(client.font, Component.literal(line), x + 6, yy, wp.color, true);
            yy += 10;
        }
    }
}
