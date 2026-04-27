package com.intrinsic.client.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.intrinsic.client.IntrinsicClient;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class LightLevelNumbersFeature {
    private static final int RADIUS_XZ = 16;
    private static final int RADIUS_Y = 4;
    private static final int REFRESH_TICKS = 10;

    private static final List<Cached> CACHE = new ArrayList<>();
    private static int tickCounter = 0;
    private static long lastRebuildTick = -1000;
    private static double lastScanX = Double.NaN, lastScanY = Double.NaN, lastScanZ = Double.NaN;

    private LightLevelNumbersFeature() {}

    public static void register() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(ctx -> {
            if (!Feature.LIGHT_LEVEL_NUMBERS.isEnabled()) return;
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.level == null) return;

            maybeRebuild(client);
            if (CACHE.isEmpty()) return;

            LevelRenderState state = ctx.levelState();
            if (state == null) return;
            CameraRenderState cam = state.cameraRenderState;
            if (cam == null || !cam.initialized) return;
            Vec3 camPos = cam.pos;
            if (camPos == null) return;

            Font font = client.font;
            MultiBufferSource.BufferSource immediate = client.renderBuffers().bufferSource();
            float yawRad = (float) Math.toRadians(cam.yRot);
            float pitchRad = (float) Math.toRadians(cam.xRot);

            for (Cached c : CACHE) {
                String text = Integer.toString(c.lightLevel);
                int color = c.lightLevel == 0 ? 0xFFFF4040
                        : c.lightLevel < 8 ? 0xFFFFD040
                        : 0xFF40FF60;
                float x = c.x + 0.5f;
                float y = c.y + 1.02f;
                float z = c.z + 0.5f;

                PoseStack matrices = new PoseStack();
                matrices.pushPose();
                matrices.translate(x - camPos.x, y - camPos.y, z - camPos.z);
                matrices.mulPose(new org.joml.Quaternionf().rotateY(-yawRad));
                matrices.mulPose(new org.joml.Quaternionf().rotateX(pitchRad));
                matrices.scale(-0.025f, -0.025f, 0.025f);
                Matrix4f mat = matrices.last().pose();
                int half = font.width(text) / 2;
                font.drawInBatch(text, -half, 0, color, false, mat, immediate,
                        Font.DisplayMode.SEE_THROUGH, 0x40000000, 0x00F000F0);
                matrices.popPose();
            }
            immediate.endBatch();
        });
    }

    public static void tick(Minecraft client) {
        tickCounter++;
    }

    private static void maybeRebuild(Minecraft client) {
        if (client.player == null) return;
        double px = client.player.getX();
        double py = client.player.getY();
        double pz = client.player.getZ();
        long now = tickCounter;
        boolean dueByTick = (now - lastRebuildTick) >= REFRESH_TICKS;
        double dxp = px - lastScanX, dyp = py - lastScanY, dzp = pz - lastScanZ;
        boolean dueByMove = Double.isNaN(lastScanX)
                || dxp * dxp + dyp * dyp + dzp * dzp >= 16.0;
        if (!dueByTick && !dueByMove) return;
        rebuild(client);
        lastRebuildTick = now;
        lastScanX = px;
        lastScanY = py;
        lastScanZ = pz;
    }

    private static void rebuild(Minecraft client) {
        CACHE.clear();
        ClientLevel world = client.level;
        if (world == null || client.player == null) return;
        BlockPos center = client.player.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
        for (int dx = -RADIUS_XZ; dx <= RADIUS_XZ; dx++) {
            for (int dz = -RADIUS_XZ; dz <= RADIUS_XZ; dz++) {
                for (int dy = -RADIUS_Y; dy <= RADIUS_Y; dy++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (!SpawnRules.isSpawnable(world, cursor, below, above)) continue;
                    int light = world.getBrightness(LightLayer.BLOCK, cursor);
                    CACHE.add(new Cached(cursor.getX(), cursor.getY() - 1, cursor.getZ(), light));
                }
            }
        }
    }

    private record Cached(int x, int y, int z, int lightLevel) {}
}
