package com.intrinsic.client.feature;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class BeaconRangeFeature {
    private static final int DISCOVERY_RADIUS = 128;
    private static final int DISCOVERY_CHUNK_RADIUS = (DISCOVERY_RADIUS + 15) >> 4;
    private static final int DROP_RADIUS = DISCOVERY_RADIUS + 16;
    private static final int RESCAN_INTERVAL_TICKS = 20;

    private static final Map<BlockPos, Integer> tracked = new HashMap<>();
    private static int tickCounter = 0;

    private BeaconRangeFeature() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(BeaconRangeFeature::tick);
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(ctx -> {
            if (!Feature.BEACON_RANGE.isEnabled()) return;
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.level == null) return;
            if (tracked.isEmpty()) return;

            LevelRenderState state = ctx.levelState();
            if (state == null) return;
            CameraRenderState cam = state.cameraRenderState;
            if (cam == null || !cam.initialized || cam.pos == null) return;
            Vec3 camPos = cam.pos;

            PoseStack matrices = new PoseStack();
            matrices.pushPose();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
            Matrix4f mat = matrices.last().pose();

            MultiBufferSource.BufferSource immediate =
                    client.renderBuffers().bufferSource();
            VertexConsumer vc = immediate.getBuffer(RenderTypes.debugQuads());

            for (Map.Entry<BlockPos, Integer> e : tracked.entrySet()) {
                int range = rangeFor(e.getValue());
                if (range <= 0) continue;
                BlockPos p = e.getKey();
                float cx = p.getX() + 0.5f;
                float cy = p.getY() + 0.5f;
                float cz = p.getZ() + 0.5f;
                drawBox(vc, mat,
                        cx - range, cy - range, cz - range,
                        cx + range, cy + range, cz + range,
                        85, 200, 255, 40);
            }

            immediate.endBatch(RenderTypes.debugQuads());
            matrices.popPose();
        });
    }

    private static void tick(Minecraft client) {
        if (!Feature.BEACON_RANGE.isEnabled()) {
            if (!tracked.isEmpty()) tracked.clear();
            return;
        }
        if (client.player == null || client.level == null) {
            if (!tracked.isEmpty()) tracked.clear();
            return;
        }

        tickCounter++;
        if (tickCounter % RESCAN_INTERVAL_TICKS != 0) return;

        rescan(client);
    }

    private static void rescan(Minecraft client) {
        ClientLevel level = client.level;
        if (level == null || client.player == null) return;

        double px = client.player.getX();
        double py = client.player.getY();
        double pz = client.player.getZ();
        int playerCx = ((int) Math.floor(px)) >> 4;
        int playerCz = ((int) Math.floor(pz)) >> 4;

        Iterator<Map.Entry<BlockPos, Integer>> it = tracked.entrySet().iterator();
        while (it.hasNext()) {
            BlockPos p = it.next().getKey();
            double dx = p.getX() + 0.5 - px;
            double dy = p.getY() + 0.5 - py;
            double dz = p.getZ() + 0.5 - pz;
            if (dx * dx + dy * dy + dz * dz > (double) DROP_RADIUS * DROP_RADIUS) {
                it.remove();
            }
        }

        for (int cx = playerCx - DISCOVERY_CHUNK_RADIUS; cx <= playerCx + DISCOVERY_CHUNK_RADIUS; cx++) {
            for (int cz = playerCz - DISCOVERY_CHUNK_RADIUS; cz <= playerCz + DISCOVERY_CHUNK_RADIUS; cz++) {
                if (!level.hasChunk(cx, cz)) continue;
                LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
                if (chunk == null) continue;
                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    if (!(entry.getValue() instanceof BeaconBlockEntity)) continue;
                    BlockPos bp = entry.getKey();
                    double ddx = bp.getX() + 0.5 - px;
                    double ddy = bp.getY() + 0.5 - py;
                    double ddz = bp.getZ() + 0.5 - pz;
                    if (ddx * ddx + ddy * ddy + ddz * ddz > (double) DISCOVERY_RADIUS * DISCOVERY_RADIUS) continue;
                    int tier = computeTier(level, bp);
                    if (tier <= 0) {
                        tracked.remove(bp);
                    } else {
                        tracked.put(bp.immutable(), tier);
                    }
                }
            }
        }

    }

    private static int computeTier(ClientLevel world, BlockPos beacon) {
        int tier = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int layer = 1; layer <= 4; layer++) {
            int baseY = beacon.getY() - layer;
            boolean complete = true;
            for (int dx = -layer; dx <= layer && complete; dx++) {
                for (int dz = -layer; dz <= layer && complete; dz++) {
                    cursor.set(beacon.getX() + dx, baseY, beacon.getZ() + dz);
                    if (!isBeaconBase(world.getBlockState(cursor).getBlock())) complete = false;
                }
            }
            if (complete) tier = layer;
            else break;
        }
        return tier;
    }

    private static boolean isBeaconBase(Block b) {
        return b == Blocks.IRON_BLOCK || b == Blocks.GOLD_BLOCK
                || b == Blocks.DIAMOND_BLOCK || b == Blocks.EMERALD_BLOCK
                || b == Blocks.NETHERITE_BLOCK;
    }

    private static int rangeFor(int tier) {
        return switch (tier) {
            case 1 -> 20;
            case 2 -> 30;
            case 3 -> 40;
            case 4 -> 50;
            default -> 0;
        };
    }

    private static void drawBox(VertexConsumer vc, Matrix4f mat,
                                 float x0, float y0, float z0,
                                 float x1, float y1, float z1,
                                 int r, int g, int b, int a) {
        vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);

        vc.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);

        vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);

        vc.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);

        vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);

        vc.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
    }
}
