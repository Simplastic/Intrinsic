package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public class BiomeHud {
    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        if (client.player == null || client.level == null) return;

        BlockPos pos = client.player.blockPosition();
        Holder<Biome> biomeEntry = client.level.getBiome(pos);

        String biomeName = biomeEntry.unwrapKey()
                .map(key -> formatBiomeName(key.identifier().getPath()))
                .orElse("Unknown");

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int x = HudPositions.x("biome", sw);
        int y = HudPositions.y("biome", sh);
        context.text(client.font, "Biome: " + biomeName, x, y, 0xFF55FF55, true);
    }

    private static String formatBiomeName(String raw) {
        StringBuilder sb = new StringBuilder();
        for (String part : raw.split("_")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1));
                sb.append(' ');
            }
        }
        return sb.toString().trim();
    }
}
