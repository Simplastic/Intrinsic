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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class MobEspFeature {
    private MobEspFeature() {}

    public static void register() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(ctx -> {
            if (!Feature.MOB_ESP.isEnabled()) return;
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.level == null) return;

            LevelRenderState state = ctx.levelState();
            if (state == null) return;
            CameraRenderState cam = state.cameraRenderState;
            if (cam == null || !cam.initialized || cam.pos == null) return;
            Vec3 camPos = cam.pos;

            int radius = Math.max(4, Math.min(96, IntrinsicClient.getConfig().mobEspRadius));
            ClientLevel world = client.level;
            AABB search = client.player.getBoundingBox().inflate(radius);
            EntityTypeTest<Entity, LivingEntity> typeTest = EntityTypeTest.forClass(LivingEntity.class);
            List<LivingEntity> mobs = world.getEntities(typeTest, search,
                    e -> e != client.player && e.isAlive());
            if (mobs.isEmpty()) return;

            boolean throughWalls = IntrinsicClient.getConfig().mobEspThroughWalls;
            RenderType layer = throughWalls ? RenderTypes.linesTranslucent() : RenderTypes.lines();

            PoseStack matrices = new PoseStack();
            matrices.pushPose();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
            PoseStack.Pose pose = matrices.last();

            MultiBufferSource.BufferSource immediate =
                    client.renderBuffers().bufferSource();
            VertexConsumer vc = immediate.getBuffer(layer);

            for (LivingEntity e : mobs) {
                drawLineBox(vc, pose, e.getBoundingBox(), colorFor(e));
            }

            immediate.endBatch(layer);
            matrices.popPose();
        });
    }

    private static int colorFor(Entity e) {
        if (e instanceof Monster) return 0xFFFF4444;
        if (e instanceof AgeableMob) return 0xFF55FF55;
        return 0xFFFFDD55;
    }

    private static void drawLineBox(VertexConsumer vc, PoseStack.Pose pose, AABB box, int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;

        edge(vc, pose, x0, y0, z0, x1, y0, z0, 1, 0, 0, r, g, b, a);
        edge(vc, pose, x0, y1, z0, x1, y1, z0, 1, 0, 0, r, g, b, a);
        edge(vc, pose, x0, y0, z1, x1, y0, z1, 1, 0, 0, r, g, b, a);
        edge(vc, pose, x0, y1, z1, x1, y1, z1, 1, 0, 0, r, g, b, a);

        edge(vc, pose, x0, y0, z0, x0, y1, z0, 0, 1, 0, r, g, b, a);
        edge(vc, pose, x1, y0, z0, x1, y1, z0, 0, 1, 0, r, g, b, a);
        edge(vc, pose, x0, y0, z1, x0, y1, z1, 0, 1, 0, r, g, b, a);
        edge(vc, pose, x1, y0, z1, x1, y1, z1, 0, 1, 0, r, g, b, a);

        edge(vc, pose, x0, y0, z0, x0, y0, z1, 0, 0, 1, r, g, b, a);
        edge(vc, pose, x1, y0, z0, x1, y0, z1, 0, 0, 1, r, g, b, a);
        edge(vc, pose, x0, y1, z0, x0, y1, z1, 0, 0, 1, r, g, b, a);
        edge(vc, pose, x1, y1, z0, x1, y1, z1, 0, 0, 1, r, g, b, a);
    }

    private static void edge(VertexConsumer vc, PoseStack.Pose pose,
                              float ax, float ay, float az, float bx, float by, float bz,
                              float nx, float ny, float nz,
                              int r, int g, int b, int a) {
        vc.addVertex(pose, ax, ay, az).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(2.0f);
        vc.addVertex(pose, bx, by, bz).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(2.0f);
    }
}
