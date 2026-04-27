package com.intrinsic.client.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class CropMaturityFeature {
    private static final int REFRESH_TICKS = 10;
    private static final double REFRESH_MOVE_SQR = 4.0 * 4.0;

    private static int tickCounter = 0;
    private static int lastRebuildTick = Integer.MIN_VALUE;
    private static double lastCenterX = Double.NaN;
    private static double lastCenterY = Double.NaN;
    private static double lastCenterZ = Double.NaN;
    private static final List<CropMarker> markers = new ArrayList<>();

    private CropMaturityFeature() {}

    public static void register() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(ctx -> {
            if (!Feature.CROP_MATURITY.isEnabled()) return;
            if (markers.isEmpty()) return;
            LevelRenderState state = ctx.levelState();
            if (state == null) return;
            CameraRenderState cam = state.cameraRenderState;
            if (cam == null || !cam.initialized || cam.pos == null) return;

            Vec3 camPos = cam.pos;
            PoseStack matrices = new PoseStack();
            matrices.pushPose();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
            Matrix4f mat = matrices.last().pose();

            MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer vc = immediate.getBuffer(RenderTypes.debugQuads());

            for (CropMarker m : markers) {
                drawMarker(vc, mat, m);
            }

            immediate.endBatch(RenderTypes.debugQuads());
            matrices.popPose();
        });
    }

    public static void tick(Minecraft client) {
        if (!Feature.CROP_MATURITY.isEnabled()) {
            if (!markers.isEmpty()) markers.clear();
            return;
        }
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        if (player == null || level == null) return;

        tickCounter++;
        double px = player.getX(), py = player.getY(), pz = player.getZ();
        double dx = px - lastCenterX, dy = py - lastCenterY, dz = pz - lastCenterZ;
        boolean moved = Double.isNaN(lastCenterX) || (dx * dx + dy * dy + dz * dz) >= REFRESH_MOVE_SQR;
        if (!moved && (tickCounter - lastRebuildTick) < REFRESH_TICKS) return;

        lastCenterX = px;
        lastCenterY = py;
        lastCenterZ = pz;
        lastRebuildTick = tickCounter;
        rebuild(client, level, player);
    }

    private static void rebuild(Minecraft client, ClientLevel level, LocalPlayer player) {
        markers.clear();
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        int radius = Math.max(4, Math.min(48, cfg.cropMaturityRadius));
        int cx = (int) Math.floor(player.getX());
        int cy = (int) Math.floor(player.getY());
        int cz = (int) Math.floor(player.getZ());

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -6; y <= 6; y++) {
                    cursor.set(cx + x, cy + y, cz + z);
                    BlockState state = level.getBlockState(cursor);
                    Block block = state.getBlock();
                    Integer age = cropAge(state, block);
                    if (age == null) continue;
                    int max = cropMax(block);
                    if (max <= 0) continue;
                    boolean mature = age >= max;
                    markers.add(new CropMarker(cursor.getX(), cursor.getY(), cursor.getZ(), mature));
                }
            }
        }
    }

    private static Integer cropAge(BlockState state, Block block) {
        if (block instanceof CropBlock crop) return crop.getAge(state);
        if (block == Blocks.NETHER_WART) {
            return state.getValue(getAgeProp(block));
        }
        if (block == Blocks.BEETROOTS) {
            return state.getValue(getAgeProp(block));
        }
        return null;
    }

    private static int cropMax(Block block) {
        if (block instanceof CropBlock crop) return crop.getMaxAge();
        if (block == Blocks.NETHER_WART) return 3;
        if (block == Blocks.BEETROOTS) return 3;
        return -1;
    }

    private static IntegerProperty getAgeProp(Block block) {
        if (block == Blocks.NETHER_WART) return net.minecraft.world.level.block.NetherWartBlock.AGE;
        return net.minecraft.world.level.block.BeetrootBlock.AGE;
    }

    private static void drawMarker(VertexConsumer vc, Matrix4f mat, CropMarker m) {
        int r, g, b;
        if (m.mature) { r = 80; g = 220; b = 80; }
        else { r = 230; g = 220; b = 60; }
        int a = 180;
        float size = 0.12f;
        float cx = m.x + 0.5f, cy = m.y + 1.05f, cz = m.z + 0.5f;
        float x0 = cx - size, x1 = cx + size;
        float z0 = cz - size, z1 = cz + size;

        vc.addVertex(mat, x0, cy, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x0, cy, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x1, cy, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x1, cy, z0).setColor(r, g, b, a);
    }

    private record CropMarker(int x, int y, int z, boolean mature) {}
}
