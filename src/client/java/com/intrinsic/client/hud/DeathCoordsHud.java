package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import com.intrinsic.client.feature.WaypointsFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class DeathCoordsHud {
    private static boolean wasAlive = true;

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            wasAlive = true;
            return;
        }

        boolean isAlive = client.player.isAlive();
        if (wasAlive && !isAlive) {
            IntrinsicConfig config = IntrinsicClient.getConfig();
            config.lastDeathX = client.player.getX();
            config.lastDeathY = client.player.getY();
            config.lastDeathZ = client.player.getZ();
            config.lastDeathDimension = client.level.dimension().identifier().toString();
            config.hasDeathCoords = true;

            IntrinsicConfig.DeathEntry entry = new IntrinsicConfig.DeathEntry(
                    config.lastDeathX, config.lastDeathY, config.lastDeathZ,
                    config.lastDeathDimension, System.currentTimeMillis());
            config.deathLog.add(0, entry);
            while (config.deathLog.size() > 5) config.deathLog.remove(config.deathLog.size() - 1);

            config.save();

            String msg = String.format("[Intrinsic] Death at X: %.0f, Y: %.0f, Z: %.0f (%s)",
                    config.lastDeathX, config.lastDeathY, config.lastDeathZ, config.lastDeathDimension);
            client.player.sendSystemMessage(Component.literal(msg).withStyle(s -> s.withColor(0xFF5555)));

            WaypointsFeature.onPlayerDeath(config.lastDeathX, config.lastDeathY, config.lastDeathZ, config.lastDeathDimension);
        }
        wasAlive = isAlive;
    }

    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        IntrinsicConfig config = IntrinsicClient.getConfig();
        if (!config.hasDeathCoords) return;

        String text = String.format("Last death: %.0f, %.0f, %.0f [%s]",
                config.lastDeathX, config.lastDeathY, config.lastDeathZ,
                getShortDimension(config.lastDeathDimension));

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int x = HudPositions.x("deathCoords", sw);
        int y = HudPositions.y("deathCoords", sh);
        context.text(client.font, text, x, y, 0xFFFF5555, true);
    }

    private static String getShortDimension(String dimension) {
        if (dimension.contains("overworld")) return "Overworld";
        if (dimension.contains("the_nether")) return "Nether";
        if (dimension.contains("the_end")) return "End";
        return dimension;
    }
}
