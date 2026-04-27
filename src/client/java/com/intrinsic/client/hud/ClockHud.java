package com.intrinsic.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ClockHud {
    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        if (client.level == null) return;

        long dayTime = client.level.getOverworldClockTime() % 24000;
        int hours = (int) ((dayTime / 1000 + 6) % 24);
        int minutes = (int) ((dayTime % 1000) * 60 / 1000);

        String period = hours >= 12 ? "PM" : "AM";
        int displayHour = hours % 12;
        if (displayHour == 0) displayHour = 12;

        String text = String.format("%d:%02d %s", displayHour, minutes, period);
        boolean isDayTime = hours >= 6 && hours < 18;
        int color = isDayTime ? 0xFFFFFF55 : 0xFF5555FF;

        int tw = client.font.width(text);
        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("clock");
        int x = HudPositions.x("clock", sw) + a.width - tw;
        int y = HudPositions.y("clock", sh);
        context.text(client.font, text, x, y, color, true);
    }
}
