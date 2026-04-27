package com.intrinsic.client.hud;

import com.intrinsic.client.gui.widget.ThemeColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public final class XpProgressHud {
    private XpProgressHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;

        int level = player.experienceLevel;
        int nextTotal = player.getXpNeededForNextLevel();
        int current = (int) Math.floor(player.experienceProgress * nextTotal);
        int toGo = Math.max(0, nextTotal - current);

        String msg = "Lv " + level + "  \u00b7  " + current + "/" + nextTotal + " XP  \u00b7  " + toGo + " to go";

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int x = HudPositions.x("xpProgress", sw);
        int y = HudPositions.y("xpProgress", sh);

        int tw = client.font.width(msg);
        ctx.text(client.font, Component.literal(msg), x, y, ThemeColors.TEXT, true);
        if (tw > 0) {
            int barW = Math.max(tw, 100);
            ctx.fill(x, y + 10, x + barW, y + 12, 0x60000000);
            int filled = (int) (barW * player.experienceProgress);
            ctx.fill(x, y + 10, x + filled, y + 12, 0xFF3BD67A);
        }
    }
}
