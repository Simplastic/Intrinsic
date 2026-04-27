package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import com.intrinsic.client.util.SlimeChunkUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.ChunkPos;

public class SlimeChunkHud {
    private static final int RADIUS = 4; // 9x9 chunks window
    private static final int CELL = 7;

    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        IntrinsicConfig config = IntrinsicClient.getConfig();
        if (client.player == null || client.level == null) return;
        if (!client.level.dimension().identifier().getPath().equals("overworld")) return;

        long seed = resolveSeed(client, config);

        ChunkPos chunkPos = client.player.chunkPosition();
        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();

        int mapSize = (RADIUS * 2 + 1) * CELL;
        int mapX = HudPositions.x("slimeChunkMap", sw);
        int mapY = HudPositions.y("slimeChunkMap", sh);

        context.fill(mapX - 1, mapY - 1, mapX + mapSize + 1, mapY + mapSize + 1, 0xCC000000);

        if (seed == 0L) {
            String msg = "Slime: set seed";
            String sub = "/iseed <n>";
            context.text(client.font, msg, mapX + 4, mapY + 4, 0xFFAAAAAA, true);
            context.text(client.font, sub, mapX + 4, mapY + 16, 0xFF888888, true);
        } else {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                    int cx = chunkPos.x() + dx;
                    int cz = chunkPos.z() + dz;
                    int px = mapX + (dx + RADIUS) * CELL;
                    int py = mapY + (dz + RADIUS) * CELL;
                    if (SlimeChunkUtil.isSlimeChunk(seed, cx, cz)) {
                        context.fill(px, py, px + CELL, py + CELL, 0xFF55CC44);
                    } else {
                        context.fill(px + 1, py + 1, px + CELL - 1, py + CELL - 1, 0xFF333333);
                    }
                }
            }
            // player marker in center
            int pcx = mapX + RADIUS * CELL;
            int pcy = mapY + RADIUS * CELL;
            context.fill(pcx + 2, pcy + 2, pcx + CELL - 2, pcy + CELL - 2, 0xFFFFFFFF);
            context.fill(pcx + 3, pcy + 1, pcx + 4, pcy + CELL - 1, 0xFFFF3333);
            context.fill(pcx + 1, pcy + 3, pcx + CELL - 1, pcy + 4, 0xFFFF3333);
        }
    }

    private static long resolveSeed(Minecraft client, IntrinsicConfig config) {
        if (config.worldSeed != 0L) return config.worldSeed;
        // Try to read seed from the integrated server (singleplayer only)
        if (client.getSingleplayerServer() != null) {
            try {
                long s = client.getSingleplayerServer().overworld().getSeed();
                config.worldSeed = s;
                return s;
            } catch (Throwable ignored) {
            }
        }
        return 0L;
    }
}
