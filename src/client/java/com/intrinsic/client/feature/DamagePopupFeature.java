package com.intrinsic.client.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Floating damage numbers on entities taking damage. Pure client-side delta
// tracking — each tick we compare getHealth() against the previous snapshot
// and emit a short-lived world-space popup when it drops.
public final class DamagePopupFeature {
    private static final int LIFETIME_TICKS = 30;
    private static final float RISE_PER_TICK = 0.02f;
    // Minimum drop to display — filters regeneration flicker and synthetic 0.01 updates.
    private static final float MIN_DELTA = 0.5f;
    private static final int GC_INTERVAL_TICKS = 10;

    private static final Map<UUID, Float> PREV_HEALTH = new HashMap<>();
    private static final List<Popup> ACTIVE = new ArrayList<>();
    private static int tickCounter = 0;

    private DamagePopupFeature() {}

    public static void register() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(ctx -> {
            if (!Feature.DAMAGE_POPUP.isEnabled()) return;
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.level == null) return;
            if (ACTIVE.isEmpty()) return;

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
            float partial = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);

            for (Popup p : ACTIVE) {
                float ageTicks = (tickCounter - p.spawnTick) + partial;
                if (ageTicks < 0) ageTicks = 0;
                float life = ageTicks / LIFETIME_TICKS;
                if (life >= 1f) continue;
                int alpha = (int) (255 * (1f - life)) & 0xFF;
                if (alpha < 8) continue;

                int rgb = p.amount >= 8f ? 0x00FF30A0 : 0x00FF6060;
                int color = (alpha << 24) | rgb;
                String text = "-" + formatAmount(p.amount);

                float x = (float) p.x;
                float y = (float) (p.y + ageTicks * RISE_PER_TICK);
                float z = (float) p.z;

                PoseStack matrices = new PoseStack();
                matrices.pushPose();
                matrices.translate(x - camPos.x, y - camPos.y, z - camPos.z);
                matrices.mulPose(new org.joml.Quaternionf().rotateY(-yawRad));
                matrices.mulPose(new org.joml.Quaternionf().rotateX(pitchRad));
                matrices.scale(-0.03f, -0.03f, 0.03f);
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
        if (client.level == null || client.player == null) return;

        // Expire old popups.
        Iterator<Popup> it = ACTIVE.iterator();
        while (it.hasNext()) {
            if (tickCounter - it.next().spawnTick >= LIFETIME_TICKS) it.remove();
        }

        if (!Feature.DAMAGE_POPUP.isEnabled()) {
            // Still drain snapshots when the feature is off so we don't hold
            // references across dimension/server swaps.
            if ((tickCounter & 63) == 0) PREV_HEALTH.clear();
            return;
        }

        ClientLevel level = client.level;
        Set<UUID> seen = (tickCounter % GC_INTERVAL_TICKS) == 0 ? new HashSet<>() : null;

        for (Entity e : level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity le)) continue;
            if (!le.isAlive()) continue;
            UUID id = le.getUUID();
            if (seen != null) seen.add(id);
            float cur = le.getHealth();
            Float prev = PREV_HEALTH.put(id, cur);
            if (prev == null) continue;
            float delta = prev - cur;
            if (delta < MIN_DELTA) continue;
            ACTIVE.add(new Popup(
                    le.getX(),
                    le.getY() + le.getBbHeight() + 0.4,
                    le.getZ(),
                    delta,
                    tickCounter));
        }

        if (seen != null) PREV_HEALTH.keySet().retainAll(seen);
    }

    private static String formatAmount(float amount) {
        if (Math.abs(amount - Math.round(amount)) < 0.05f) {
            return Integer.toString(Math.round(amount));
        }
        return String.format("%.1f", amount);
    }

    private record Popup(double x, double y, double z, float amount, int spawnTick) {}
}
