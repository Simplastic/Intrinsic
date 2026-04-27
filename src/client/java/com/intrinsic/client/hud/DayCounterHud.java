package com.intrinsic.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DayCounterHud {
    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        if (client.level == null) return;

        long totalTime = client.level.getOverworldClockTime();
        long day = totalTime / 24000 + 1;

        String text = "Day " + day;
        int tw = client.font.width(text);
        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("dayCounter");
        int x = HudPositions.x("dayCounter", sw) + a.width - tw;
        int y = HudPositions.y("dayCounter", sh);
        context.text(client.font, text, x, y, 0xFFFFFFFF, true);
    }
}
