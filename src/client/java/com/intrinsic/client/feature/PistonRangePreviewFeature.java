package com.intrinsic.client.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

// Ghost wireframe along the course a piston would push if it extended now.
// Reads client.hitResult to identify a non-extended PistonBaseBlock and walks
// up to 12 cells along its facing direction. Color per cell reflects
// movability so redstone builders can see where a push would stall.
public final class PistonRangePreviewFeature {
    private static final int PUSH_LIMIT = 12;

    private PistonRangePreviewFeature() {}

    public static void register() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(ctx -> {
            if (!Feature.PISTON_RANGE_PREVIEW.isEnabled()) return;
            Minecraft client = Minecraft.getInstance();
            LocalPlayer player = client.player;
            ClientLevel level = client.level;
            if (player == null || level == null) return;

            if (!(client.hitResult instanceof BlockHitResult hit)) return;
            if (hit.getType() != HitResult.Type.BLOCK) return;

            BlockPos pistonPos = hit.getBlockPos();
            BlockState pistonState = level.getBlockState(pistonPos);
            if (!(pistonState.getBlock() instanceof PistonBaseBlock)) return;
            if (pistonState.getValue(PistonBaseBlock.EXTENDED)) return;

            Direction facing = pistonState.getValue(DirectionalBlock.FACING);

            LevelRenderState state = ctx.levelState();
            if (state == null) return;
            CameraRenderState cam = state.cameraRenderState;
            if (cam == null || !cam.initialized || cam.pos == null) return;
            Vec3 camPos = cam.pos;

            PoseStack matrices = new PoseStack();
            matrices.pushPose();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
            PoseStack.Pose pose = matrices.last();

            RenderType layer = RenderTypes.lines();
            MultiBufferSource.BufferSource immediate = client.renderBuffers().bufferSource();
            VertexConsumer vc = immediate.getBuffer(layer);

            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int i = 1; i <= PUSH_LIMIT; i++) {
                cursor.set(
                        pistonPos.getX() + facing.getStepX() * i,
                        pistonPos.getY() + facing.getStepY() * i,
                        pistonPos.getZ() + facing.getStepZ() * i
                );
                BlockState cellState = level.getBlockState(cursor);
                PushReaction reaction = cellState.getPistonPushReaction();

                int r, g, b;
                boolean stop = false;
                if (cellState.isAir() || cellState.canBeReplaced()) {
                    r = 0x60; g = 0xE0; b = 0x80;
                } else if (reaction == PushReaction.BLOCK) {
                    r = 0xE0; g = 0x40; b = 0x40;
                    stop = true;
                } else {
                    r = 0xE0; g = 0xE0; b = 0x60;
                }

                drawBoxEdges(vc, pose,
                        cursor.getX(), cursor.getY(), cursor.getZ(),
                        cursor.getX() + 1f, cursor.getY() + 1f, cursor.getZ() + 1f,
                        r, g, b, 0xC0);

                if (stop) break;
            }

            immediate.endBatch(layer);
            matrices.popPose();
        });
    }

    private static void drawBoxEdges(VertexConsumer vc, PoseStack.Pose pose,
                                      float x0, float y0, float z0,
                                      float x1, float y1, float z1,
                                      int r, int g, int b, int a) {
        edge(vc, pose, x0, y0, z0, x1, y0, z0, 1, 0, 0, r, g, b, a);
        edge(vc, pose, x1, y0, z0, x1, y0, z1, 0, 0, 1, r, g, b, a);
        edge(vc, pose, x1, y0, z1, x0, y0, z1, 1, 0, 0, r, g, b, a);
        edge(vc, pose, x0, y0, z1, x0, y0, z0, 0, 0, 1, r, g, b, a);
        edge(vc, pose, x0, y1, z0, x1, y1, z0, 1, 0, 0, r, g, b, a);
        edge(vc, pose, x1, y1, z0, x1, y1, z1, 0, 0, 1, r, g, b, a);
        edge(vc, pose, x1, y1, z1, x0, y1, z1, 1, 0, 0, r, g, b, a);
        edge(vc, pose, x0, y1, z1, x0, y1, z0, 0, 0, 1, r, g, b, a);
        edge(vc, pose, x0, y0, z0, x0, y1, z0, 0, 1, 0, r, g, b, a);
        edge(vc, pose, x1, y0, z0, x1, y1, z0, 0, 1, 0, r, g, b, a);
        edge(vc, pose, x1, y0, z1, x1, y1, z1, 0, 1, 0, r, g, b, a);
        edge(vc, pose, x0, y0, z1, x0, y1, z1, 0, 1, 0, r, g, b, a);
    }

    private static void edge(VertexConsumer vc, PoseStack.Pose pose,
                              float ax, float ay, float az,
                              float bx, float by, float bz,
                              float nx, float ny, float nz,
                              int r, int g, int b, int a) {
        vc.addVertex(pose, ax, ay, az).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(2.0f);
        vc.addVertex(pose, bx, by, bz).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(2.0f);
    }
}
