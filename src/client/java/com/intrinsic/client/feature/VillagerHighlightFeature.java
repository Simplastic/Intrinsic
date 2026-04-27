package com.intrinsic.client.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.intrinsic.client.hud.VillagerTradesHud;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class VillagerHighlightFeature {
    private VillagerHighlightFeature() {}

    public static void register() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(ctx -> {
            if (!Feature.VILLAGER_TRADES.isEnabled()) return;
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.level == null) return;

            Villager target = VillagerTradesHud.currentTarget();
            if (target == null || !target.isAlive()) return;

            LevelRenderState state = ctx.levelState();
            if (state == null) return;
            CameraRenderState cam = state.cameraRenderState;
            if (cam == null || !cam.initialized || cam.pos == null) return;
            Vec3 camPos = cam.pos;

            AABB box = target.getBoundingBox();
            float pad = 0.05f;
            float x0 = (float) box.minX - pad, x1 = (float) box.maxX + pad;
            float y0 = (float) box.minY - pad, y1 = (float) box.maxY + pad;
            float z0 = (float) box.minZ - pad, z1 = (float) box.maxZ + pad;

            PoseStack matrices = new PoseStack();
            matrices.pushPose();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
            Matrix4f mat = matrices.last().pose();

            MultiBufferSource.BufferSource immediate = client.renderBuffers().bufferSource();
            VertexConsumer vc = immediate.getBuffer(RenderTypes.debugQuads());

            drawBox(vc, mat, x0, y0, z0, x1, y1, z1, 255, 213, 79, 60);

            immediate.endBatch(RenderTypes.debugQuads());
            matrices.popPose();
        });
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
