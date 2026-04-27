package com.intrinsic.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class BedWarningHud {
    private BedWarningHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        ClientLevel level = client.level;
        if (level == null || client.player == null) return;

        String dim = level.dimension().identifier().getPath();
        if (!"the_nether".equals(dim) && !"the_end".equals(dim)) return;

        HitResult hr = client.hitResult;
        if (!(hr instanceof BlockHitResult bhr) || bhr.getType() == HitResult.Type.MISS) return;

        BlockState state = level.getBlockState(bhr.getBlockPos());
        if (!(state.getBlock() instanceof BedBlock)) return;

        String text = "\u26A0 EXPLODES IF USED \u26A0";
        int color = HudStyle.blink(500) ? HudStyle.WARN_RED : 0xFF882222;

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("bedWarning");
        int x = HudPositions.x("bedWarning", sw);
        int y = HudPositions.y("bedWarning", sh);
        HudStyle.warningBanner(ctx, client.font, text, x, y, a.width, color);
    }
}
