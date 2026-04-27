package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import com.intrinsic.client.gui.widget.ThemeColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public final class DeathLogHud {
    private DeathLogHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        List<IntrinsicConfig.DeathEntry> log = cfg.deathLog;
        if (log == null || log.isEmpty()) return;

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("deathLog");
        int cardW = a.width;
        int padX = 6;
        int padY = 3;
        int titleH = 12;
        int rowH = 10;
        int shown = Math.min(5, log.size());
        int contentHeight = titleH + rowH * shown + padY;

        int x0 = HudPositions.x("deathLog", sw);
        int y0 = HudPositions.y("deathLog", sh);
        int x1 = x0 + cardW;
        int y1 = y0 + contentHeight;

        HudStyle.card(ctx, x0, y0, x1 - x0, y1 - y0);

        String title = "Death Log";
        int tw = client.font.width(title);
        ctx.text(client.font, title, x0 + (cardW - tw) / 2, y0 + 3, ThemeColors.TEXT, true);

        long now = System.currentTimeMillis();
        int yy = y0 + titleH;
        for (int i = 0; i < shown; i++) {
            IntrinsicConfig.DeathEntry e = log.get(i);
            String line = String.format("%s  %.0f, %.0f, %.0f  %s",
                    shortDim(e.dimension), e.x, e.y, e.z, ago(now - e.timeMs));
            ctx.text(client.font, line, x0 + padX, yy, ThemeColors.TEXT_DIM, true);
            yy += rowH;
        }
    }

    private static String shortDim(String d) {
        if (d == null) return "?";
        if (d.contains("overworld")) return "OW";
        if (d.contains("the_nether")) return "Ne";
        if (d.contains("the_end")) return "En";
        return d;
    }

    private static String ago(long ms) {
        long s = ms / 1000;
        if (s < 60) return s + "s ago";
        long m = s / 60;
        if (m < 60) return m + "m ago";
        long h = m / 60;
        if (h < 24) return h + "h ago";
        long d = h / 24;
        return d + "d ago";
    }
}
