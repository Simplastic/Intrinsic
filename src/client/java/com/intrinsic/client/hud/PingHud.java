package com.intrinsic.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

public class PingHud {
    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        if (client.player == null) return;

        ClientPacketListener handler = client.getConnection();
        if (handler == null) return;
        PlayerInfo entry = handler.getPlayerInfo(client.player.getUUID());
        if (entry == null) return;

        int ping = entry.getLatency();
        int color = ping < 80 ? 0xFF55FF55 : ping < 200 ? 0xFFFFFF55 : 0xFFFF5555;

        String text = ping + " ms";
        int tw = client.font.width(text);
        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("ping");
        int x = HudPositions.x("ping", sw) + a.width - tw;
        int y = HudPositions.y("ping", sh);
        context.text(client.font, text, x, y, color, true);
    }
}
