package com.intrinsic.client.hud;

import com.intrinsic.client.gui.widget.ThemeColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public final class WorldStatsHud {
    private WorldStatsHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        ClientLevel level = client.level;
        if (level == null) return;

        int entities = 0;
        for (Entity ignored : level.entitiesForRendering()) entities++;
        int loadedChunks = level.getChunkSource().getLoadedChunksCount();
        int players = level.players().size();

        String lineE = "entities: " + entities;
        String lineC = "chunks: "   + loadedChunks;
        String lineP = "players: "  + players;

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("worldStats");
        int x = HudPositions.x("worldStats", sw);
        int y = HudPositions.y("worldStats", sh);

        int w = a.width;
        int h = HudStyle.HEADER_H + HudStyle.LINE_STRIDE * 3 + HudStyle.PAD_Y;
        HudStyle.card(ctx, x, y, w, h);

        int yy = y + HudStyle.PAD_Y;
        ctx.text(client.font, "World Stats", x + HudStyle.PAD_X, yy, ThemeColors.TEXT, true);
        yy += HudStyle.HEADER_H;
        ctx.text(client.font, lineE, x + HudStyle.PAD_X, yy, ThemeColors.TEXT_DIM, true);
        yy += HudStyle.LINE_STRIDE;
        ctx.text(client.font, lineC, x + HudStyle.PAD_X, yy, ThemeColors.TEXT_DIM, true);
        yy += HudStyle.LINE_STRIDE;
        ctx.text(client.font, lineP, x + HudStyle.PAD_X, yy, ThemeColors.TEXT_DIM, true);
    }
}
