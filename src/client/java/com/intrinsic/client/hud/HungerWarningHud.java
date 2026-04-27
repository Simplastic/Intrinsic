package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class HungerWarningHud {
    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        if (client.player == null) return;

        int food = client.player.getFoodData().getFoodLevel();
        if (food >= 6) return;

        long now = System.currentTimeMillis();
        boolean blink = (now / 500) % 2 == 0;
        if (!blink) return;

        String text = "\u26A0 Hungry (" + food + "/20)";
        int width = client.font.width(text);

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("hungerWarning");
        int x = HudPositions.x("hungerWarning", sw) + (a.width - width) / 2;
        int y = HudPositions.y("hungerWarning", sh);

        int color = food <= 2 ? 0xFFFF3333 : 0xFFFFAA33;
        context.text(client.font, text, x, y, color, true);
    }
}
