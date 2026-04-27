package com.intrinsic.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

public class LightLevelHud {
    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        if (client.player == null || client.level == null) return;

        BlockPos pos = client.player.blockPosition();
        int blockLight = client.level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = client.level.getBrightness(LightLayer.SKY, pos);
        int totalLight = client.level.getMaxLocalRawBrightness(pos);

        int color;
        if (blockLight == 0) {
            color = 0xFFFF5555; // mobs can spawn (1.18+ rule)
        } else if (totalLight >= 8) {
            color = 0xFF55FF55;
        } else {
            color = 0xFFFFFF55;
        }

        String text = String.format("Light: %d (B:%d S:%d)%s",
                totalLight, blockLight, skyLight,
                blockLight == 0 ? " [SPAWNABLE]" : "");
        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int x = HudPositions.x("lightLevel", sw);
        int y = HudPositions.y("lightLevel", sh);
        context.text(client.font, text, x, y, color, true);
    }
}
