package com.intrinsic.client.hud;

import com.intrinsic.client.util.TpsTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class TpsHud {
    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        if (client.level == null || client.hasSingleplayerServer()) return;
        if (!TpsTracker.hasData()) return;

        double tps = TpsTracker.getTps();
        double mspt = TpsTracker.getAverageMspt();
        int color = tps > 19.0 ? 0xFF55FF55 : tps > 15.0 ? 0xFFFFFF55 : 0xFFFF5555;

        String text = String.format("TPS: %.1f (%.0f ms)", tps, mspt);
        int tw = client.font.width(text);
        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("tps");
        int x = HudPositions.x("tps", sw) + a.width - tw;
        int y = HudPositions.y("tps", sh);
        context.text(client.font, text, x, y, color, true);
    }
}
