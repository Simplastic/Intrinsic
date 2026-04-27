package com.intrinsic.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class FpsHud {
    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        int fps = client.getFps();
        int color = fps >= 60 ? 0xFF55FF55 : fps >= 30 ? 0xFFFFFF55 : 0xFFFF5555;

        String text = fps + " FPS";
        int tw = client.font.width(text);

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("fps");
        int x = HudPositions.x("fps", sw) + a.width - tw;
        int y = HudPositions.y("fps", sh);
        context.text(client.font, text, x, y, color, true);
    }
}
