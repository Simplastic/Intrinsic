package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.gui.widget.ThemeColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class PortalCoordsHud {
    private PortalCoordsHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        ClientLevel level = client.level;
        if (level == null || client.player == null) return;
        HitResult hr = client.hitResult;
        if (!(hr instanceof BlockHitResult bhr) || bhr.getType() == HitResult.Type.MISS) return;

        BlockState state = level.getBlockState(bhr.getBlockPos());
        if (!state.is(Blocks.NETHER_PORTAL)
                && !state.is(Blocks.END_PORTAL)
                && !state.is(Blocks.END_PORTAL_FRAME)) return;

        String dim = level.dimension().identifier().getPath();
        boolean overworld = "overworld".equals(dim);
        boolean nether = "the_nether".equals(dim);
        if (!overworld && !nether) return;

        double px = client.player.getX();
        double pz = client.player.getZ();
        double tx, tz;
        String fromLabel, toLabel;
        if (overworld) {
            tx = px / 8.0;
            tz = pz / 8.0;
            fromLabel = "Overworld";
            toLabel = "Nether";
        } else {
            tx = px * 8.0;
            tz = pz * 8.0;
            fromLabel = "Nether";
            toLabel = "Overworld";
        }

        String title = "Portal Link";
        String fromRow = String.format("%s: %.1f / %.1f", fromLabel, px, pz);
        String toRow = String.format("%s: %.1f / %.1f", toLabel, tx, tz);

        int padX = 6;
        int titleH = 12;
        int rowH = 10;
        int cardH = titleH + rowH * 2 + 4;
        int titleW = client.font.width(title);
        int contentW = Math.max(titleW, Math.max(client.font.width(fromRow), client.font.width(toRow)));
        int cardW = Math.min(contentW + padX * 2, 260);

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("portalCoords");
        int anchorX = HudPositions.x("portalCoords", sw);
        int anchorY = HudPositions.y("portalCoords", sh);
        int x0 = anchorX + a.width / 2 - cardW / 2;
        int y0 = anchorY;

        HudStyle.card(ctx, x0, y0, cardW, cardH);

        int yy = y0 + 3;
        ctx.text(client.font, title, x0 + (cardW - titleW) / 2, yy, ThemeColors.TEXT, true);
        yy += titleH;
        int fw = client.font.width(fromRow);
        ctx.text(client.font, fromRow, x0 + (cardW - fw) / 2, yy, ThemeColors.TEXT_DIM, true);
        yy += rowH;
        int tw = client.font.width(toRow);
        ctx.text(client.font, toRow, x0 + (cardW - tw) / 2, yy, ThemeColors.TEXT, true);
    }
}
