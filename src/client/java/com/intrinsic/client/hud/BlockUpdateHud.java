package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.ChunkPos;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class BlockUpdateHud {
    private static final ConcurrentHashMap<Long, AtomicLong> COUNTS = new ConcurrentHashMap<>();

    public static void recordUpdate(int chunkX, int chunkZ) {
        long key = ChunkPos.pack(chunkX, chunkZ);
        COUNTS.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }

    public static void resetAll() {
        COUNTS.clear();
    }

    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        if (client.player == null) return;

        ChunkPos pos = client.player.chunkPosition();
        long key = ChunkPos.pack(pos.x(), pos.z());
        AtomicLong v = COUNTS.get(key);
        long here = v == null ? 0 : v.get();

        String text = String.format("BU chunk [%d,%d]: %d", pos.x(), pos.z(), here);
        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int x = HudPositions.x("blockUpdate", sw);
        int y = HudPositions.y("blockUpdate", sh);
        context.text(client.font, text, x, y, 0xFFFFAA66, true);
    }
}
