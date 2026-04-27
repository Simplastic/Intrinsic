package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class LightHeatmapFeature {
    private static final int RADIUS_Y = 4;
    private static final int REFRESH_TICKS = 5;
    private static final int MIN_RADIUS = 4;
    private static final int MAX_RADIUS = 48;

    private static final List<Cached> CACHE = new ArrayList<>();
    private static int tickCounter = 0;
    private static long lastRebuildTick = -1000;
    private static double lastScanX = Double.NaN, lastScanY = Double.NaN, lastScanZ = Double.NaN;

    private LightHeatmapFeature() {}

    public static void register() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(ctx -> {
            if (!IntrinsicClient.getConfig().lightHeatmap) return;
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

            PoseStack matrices = new PoseStack();
            matrices.pushPose();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
            Matrix4f mat = matrices.last().pose();

            MultiBufferSource.BufferSource immediate =
                    client.renderBuffers().bufferSource();
            VertexConsumer vc = immediate.getBuffer(RenderTypes.debugQuads());

            for (Cached c : CACHE) {
                int r, g, b;
                if (c.lightLevel == 0) { r = 255; g = 60;  b = 60; }
                else                    { r = 255; g = 220; b = 60; }
                int a = 110;
                float x = c.x;
                float y = c.y + 1.01f;
                float z = c.z;
                vc.addVertex(mat, x,     y, z    ).setColor(r, g, b, a);
                vc.addVertex(mat, x,     y, z + 1).setColor(r, g, b, a);
                vc.addVertex(mat, x + 1, y, z + 1).setColor(r, g, b, a);
                vc.addVertex(mat, x + 1, y, z    ).setColor(r, g, b, a);
            }
            immediate.endBatch(RenderTypes.debugQuads());
            matrices.popPose();
        });
    }

    public static void tick(Minecraft client) {
        tickCounter++;
    }

    public static int currentRadius() {
        int r = IntrinsicClient.getConfig().lightHeatmapRadius;
        if (r < MIN_RADIUS) return MIN_RADIUS;
        if (r > MAX_RADIUS) return MAX_RADIUS;
        return r;
    }

    public static int minRadius() { return MIN_RADIUS; }
    public static int maxRadius() { return MAX_RADIUS; }

    @SuppressWarnings("DataFlowIssue")
    private static void maybeRebuild(Minecraft client) {
        if (client.player == null || client.level == null) return;
        double px = client.player.getX();
        double py = client.player.getY();
        double pz = client.player.getZ();
        long now = tickCounter;
        int radius = currentRadius();
        double moveThreshold = Math.max(4.0, (radius * 0.5) * (radius * 0.5));
        boolean dueByTick = (now - lastRebuildTick) >= REFRESH_TICKS;
        double dxp = px - lastScanX, dyp = py - lastScanY, dzp = pz - lastScanZ;
        boolean dueByMove = Double.isNaN(lastScanX)
                || dxp * dxp + dyp * dyp + dzp * dzp >= moveThreshold;
        if (!dueByTick && !dueByMove) return;

        rebuild(client, radius);
        lastRebuildTick = now;
        lastScanX = px;
        lastScanY = py;
        lastScanZ = pz;
    }

    private static void rebuild(Minecraft client, int radiusXZ) {
        CACHE.clear();
        ClientLevel world = client.level;
        if (world == null || client.player == null) return;
        BlockPos center = client.player.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
        for (int dx = -radiusXZ; dx <= radiusXZ; dx++) {
            for (int dz = -radiusXZ; dz <= radiusXZ; dz++) {
                for (int dy = -RADIUS_Y; dy <= RADIUS_Y; dy++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (!SpawnRules.isSpawnable(world, cursor, below, above)) continue;
                    int light = world.getBrightness(LightLayer.BLOCK, cursor);
                    if (light >= 8) continue;
                    CACHE.add(new Cached(cursor.getX(), cursor.getY() - 1, cursor.getZ(), light));
                }
            }
        }
    }

    private record Cached(int x, int y, int z, int lightLevel) {}
}
