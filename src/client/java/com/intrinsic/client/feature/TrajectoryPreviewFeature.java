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
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

// Renders a dotted line showing the predicted arc of the projectile that would
// fire from the currently-held item. Uses vanilla physics (gravity + drag per
// projectile family) and stops the line on first block collision.
public final class TrajectoryPreviewFeature {
    private TrajectoryPreviewFeature() {}

    private record ProjectileProfile(double velocity, double gravity, double drag) {}

    // Vanilla constants: see AbstractArrow (drag 0.99, gravity 0.05) and
    // ThrowableProjectile (drag 0.99, gravity 0.03). Bow full-charge velocity
    // is 3.0, crossbow 3.15, trident 2.5, throwables 1.5.
    private static final ProjectileProfile ARROW_BOW      = new ProjectileProfile(3.00, 0.05, 0.99);
    private static final ProjectileProfile ARROW_CROSSBOW = new ProjectileProfile(3.15, 0.05, 0.99);
    private static final ProjectileProfile TRIDENT        = new ProjectileProfile(2.50, 0.05, 0.99);
    private static final ProjectileProfile THROWABLE      = new ProjectileProfile(1.50, 0.03, 0.99);

    public static void register() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(ctx -> {
            if (!Feature.TRAJECTORY_PREVIEW.isEnabled()) return;
            Minecraft client = Minecraft.getInstance();
            LocalPlayer player = client.player;
            ClientLevel level = client.level;
            if (player == null || level == null) return;

            ProjectileProfile profile = profileFor(player.getMainHandItem());
            if (profile == null) profile = profileFor(player.getOffhandItem());
            if (profile == null) return;

            LevelRenderState state = ctx.levelState();
            if (state == null) return;
            CameraRenderState cam = state.cameraRenderState;
            if (cam == null || !cam.initialized || cam.pos == null) return;
            Vec3 camPos = cam.pos;

            int maxTicks = Math.max(20, Math.min(400,
                    com.intrinsic.client.IntrinsicClient.getConfig().trajectoryMaxTicks));

            Vec3 eye = player.getEyePosition(1.0f);
            Vec3 look = player.getLookAngle();
            Vec3 pos = eye;
            Vec3 vel = look.scale(profile.velocity);

            PoseStack matrices = new PoseStack();
            matrices.pushPose();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
            PoseStack.Pose pose = matrices.last();

            RenderType layer = RenderTypes.lines();
            MultiBufferSource.BufferSource immediate = client.renderBuffers().bufferSource();
            VertexConsumer vc = immediate.getBuffer(layer);

            int color = 0xFFFFE070;
            int a = 0xFF, r = 0xFF, g = 0xE0, b = 0x70;

            for (int i = 0; i < maxTicks; i++) {
                Vec3 next = pos.add(vel);
                HitResult hr = level.clip(new ClipContext(
                        pos, next,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player));

                Vec3 endPoint = hr.getType() == HitResult.Type.MISS ? next : hr.getLocation();

                // one line segment per tick
                vc.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z)
                        .setColor(r, g, b, a)
                        .setNormal(pose, 0, 1, 0)
                        .setLineWidth(2.0f);
                vc.addVertex(pose, (float) endPoint.x, (float) endPoint.y, (float) endPoint.z)
                        .setColor(r, g, b, a)
                        .setNormal(pose, 0, 1, 0)
                        .setLineWidth(2.0f);

                if (hr.getType() != HitResult.Type.MISS) {
                    drawImpactMarker(vc, pose, endPoint, hr instanceof BlockHitResult ? color : 0xFFFF4040);
                    break;
                }

                pos = next;
                vel = vel.scale(profile.drag).subtract(0, profile.gravity, 0);
            }

            immediate.endBatch(layer);
            matrices.popPose();
        });
    }

    private static ProjectileProfile profileFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        Item it = stack.getItem();
        if (it instanceof BowItem) return ARROW_BOW;
        if (it instanceof CrossbowItem) return ARROW_CROSSBOW;
        if (it instanceof TridentItem) return TRIDENT;
        if (it instanceof SnowballItem) return THROWABLE;
        if (it instanceof EggItem) return THROWABLE;
        if (it instanceof EnderpearlItem) return THROWABLE;
        if (it instanceof SplashPotionItem) return THROWABLE;
        return null;
    }

    private static void drawImpactMarker(VertexConsumer vc, PoseStack.Pose pose, Vec3 p, int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        float s = 0.15f;
        float x = (float) p.x, y = (float) p.y, z = (float) p.z;
        axis(vc, pose, x - s, y, z, x + s, y, z, 1, 0, 0, r, g, b, a);
        axis(vc, pose, x, y - s, z, x, y + s, z, 0, 1, 0, r, g, b, a);
        axis(vc, pose, x, y, z - s, x, y, z + s, 0, 0, 1, r, g, b, a);
    }

    private static void axis(VertexConsumer vc, PoseStack.Pose pose,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float nx, float ny, float nz,
                             int r, int g, int b, int a) {
        vc.addVertex(pose, ax, ay, az).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(2.0f);
        vc.addVertex(pose, bx, by, bz).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(2.0f);
    }
}
