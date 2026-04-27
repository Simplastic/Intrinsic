package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.hud.HudPositions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.ChunkPos;

public class ChunkBorderFeature {
    private static final DustParticleOptions YELLOW = new DustParticleOptions(0xFFDD33, 1.0f);
    private static final DustParticleOptions BLUE = new DustParticleOptions(0x3399FF, 1.0f);
    private static final DustParticleOptions RED = new DustParticleOptions(0xFF4040, 1.0f);

    private static int tickCounter = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ChunkBorderFeature::worldTick);
    }

    private static void worldTick(Minecraft client) {
        if (!IntrinsicClient.getConfig().chunkBorders) return;
        if (client.player == null || client.level == null) return;
        tickCounter = (tickCounter + 1) & 0xFF;
        if ((tickCounter & 3) != 0) return;

        ClientLevel world = client.level;
        ChunkPos cp = client.player.chunkPosition();
        int startX = cp.getMinBlockX();
        int startZ = cp.getMinBlockZ();
        int py = (int) client.player.getY();
        int bottom = Math.max(world.getMinY(), py - 6);
        int top = Math.min(world.getMaxY(), py + 18);

        for (int y = bottom; y <= top; y += 2) {
            world.addParticle(YELLOW, startX,          y, startZ,          0, 0, 0);
            world.addParticle(YELLOW, startX + 16.0,   y, startZ,          0, 0, 0);
            world.addParticle(YELLOW, startX,          y, startZ + 16.0,   0, 0, 0);
            world.addParticle(YELLOW, startX + 16.0,   y, startZ + 16.0,   0, 0, 0);
        }

        for (int step = 2; step < 16; step += 2) {
            world.addParticle(BLUE, startX + step,  py, startZ,         0, 0, 0);
            world.addParticle(BLUE, startX + step,  py, startZ + 16.0,  0, 0, 0);
            world.addParticle(BLUE, startX,         py, startZ + step,  0, 0, 0);
            world.addParticle(BLUE, startX + 16.0,  py, startZ + step,  0, 0, 0);
        }

        for (int i = 0; i <= 16; i += 4) {
            world.addParticle(RED, startX + i, py, startZ,        0, 0, 0);
            world.addParticle(RED, startX + i, py, startZ + 16.0, 0, 0, 0);
            world.addParticle(RED, startX,        py, startZ + i, 0, 0, 0);
            world.addParticle(RED, startX + 16.0, py, startZ + i, 0, 0, 0);
        }
    }

    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        if (!IntrinsicClient.getConfig().chunkBorders) return;
        if (client.player == null || client.level == null) return;

        ChunkPos cp = client.player.chunkPosition();
        double px = client.player.getX();
        double pz = client.player.getZ();
        double dx = px - cp.getMinBlockX();
        double dz = pz - cp.getMinBlockZ();

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int ox = HudPositions.x("chunkBorders", sw);
        int oy = HudPositions.y("chunkBorders", sh);
        int size = 64;

        context.fill(ox, oy, ox + size, oy + size, 0x80000000);
        int border = 0xFFFFFF55;
        context.fill(ox, oy, ox + size, oy + 1, border);
        context.fill(ox, oy + size - 1, ox + size, oy + size, border);
        context.fill(ox, oy, ox + 1, oy + size, border);
        context.fill(ox + size - 1, oy, ox + size, oy + size, border);
        int cellsX = (int) (dx / 16.0 * size);
        int cellsZ = (int) (dz / 16.0 * size);
        int mx = ox + Math.min(size - 2, Math.max(0, cellsX));
        int mz = oy + Math.min(size - 2, Math.max(0, cellsZ));
        context.fill(mx - 1, mz - 1, mx + 2, mz + 2, 0xFFFF5555);

        String label = "[" + cp.x() + "," + cp.z() + "]";
        context.text(client.font, label, ox, oy + size + 2, 0xFFFFFFFF, true);
    }
}
