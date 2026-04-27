package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public class CoordinatesHud {
    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        if (client.player == null) return;

        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();

        String facing = getCardinalDirection(client.player.getYRot());

        String coordText = String.format("XYZ: %.1f / %.1f / %.1f [%s]", x, y, z, facing);
        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int cx = HudPositions.x("coordinates", sw);
        int cy = HudPositions.y("coordinates", sh);
        context.text(client.font, coordText, cx, cy, 0xFFFFFFFF, true);
    }

    private static String getCardinalDirection(float yaw) {
        float normalizedYaw = Mth.wrapDegrees(yaw);
        if (normalizedYaw >= -45 && normalizedYaw < 45) return "S";
        if (normalizedYaw >= 45 && normalizedYaw < 135) return "W";
        if (normalizedYaw >= 135 || normalizedYaw < -135) return "N";
        return "E";
    }
}
