package com.intrinsic.client.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public final class SpawningSphereFeature {
    private static final double INNER_RADIUS = 24.0;
    private static final double OUTER_RADIUS = 128.0;

    // UV-sphere wireframe. Stored on a unit sphere; scaled per-radius at render time.
    private static final float[][] UNIT_SEGMENTS = buildUnitWireframe(24, 24);
    // UV-sphere filled quads (4 vertices × 3 coords = 12 floats per row).
    private static final float[][] UNIT_QUADS = buildUnitQuads(24, 24);

    private SpawningSphereFeature() {}

    public static void register() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(ctx -> {
            IntrinsicConfig cfg = IntrinsicClient.getConfig();
            if (!cfg.spawnSphere) return;
            if (!cfg.spawnSphereInner && !cfg.spawnSphereOuter) return;

            Minecraft client = Minecraft.getInstance();
            LocalPlayer player = client.player;
            if (player == null || client.level == null) return;

            LevelRenderState state = ctx.levelState();
            if (state == null) return;
            CameraRenderState cam = state.cameraRenderState;
            if (cam == null || !cam.initialized || cam.pos == null) return;
            Vec3 camPos = cam.pos;

            Vec3 center = cfg.spawnSphereLocked
                    ? new Vec3(cfg.spawnSphereLockX, cfg.spawnSphereLockY, cfg.spawnSphereLockZ)
                    : player.getEyePosition(1.0f);

            PoseStack matrices = new PoseStack();
            matrices.pushPose();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
            PoseStack.Pose pose = matrices.last();

            MultiBufferSource.BufferSource immediate = client.renderBuffers().bufferSource();

            if (cfg.spawnSphereOpacity > 0) {
                RenderType fillLayer = RenderTypes.debugQuads();
                VertexConsumer vcFill = immediate.getBuffer(fillLayer);
                if (cfg.spawnSphereInner) {
                    drawSphereSurface(vcFill, pose, center, INNER_RADIUS,
                            cfg.spawnSphereInnerColor, cfg.spawnSphereOpacity);
                }
                if (cfg.spawnSphereOuter) {
                    drawSphereSurface(vcFill, pose, center, OUTER_RADIUS,
                            cfg.spawnSphereOuterColor, cfg.spawnSphereOpacity);
                }
                immediate.endBatch(fillLayer);
            }

            RenderType lineLayer = RenderTypes.lines();
            VertexConsumer vcLine = immediate.getBuffer(lineLayer);
            if (cfg.spawnSphereInner) {
                drawSphere(vcLine, pose, center, INNER_RADIUS, cfg.spawnSphereInnerColor);
            }
            if (cfg.spawnSphereOuter) {
                drawSphere(vcLine, pose, center, OUTER_RADIUS, cfg.spawnSphereOuterColor);
            }
            immediate.endBatch(lineLayer);
            matrices.popPose();
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("ispawnsphere")
                    .executes(ctx -> {
                        reply(ctx.getSource(),
                                "Usage: /ispawnsphere on | off | toggle | inner | outer | lock | move <x> <y> <z> | unlock | opacity <0-255>");
                        return 1;
                    })
                    .then(ClientCommands.literal("on").executes(c -> { set(true); reply(c.getSource(), "Spawning sphere: on"); return 1; }))
                    .then(ClientCommands.literal("off").executes(c -> { set(false); reply(c.getSource(), "Spawning sphere: off"); return 1; }))
                    .then(ClientCommands.literal("toggle").executes(c -> {
                        IntrinsicConfig cfg = IntrinsicClient.getConfig();
                        set(!cfg.spawnSphere);
                        reply(c.getSource(), "Spawning sphere: " + (cfg.spawnSphere ? "on" : "off"));
                        return 1;
                    }))
                    .then(ClientCommands.literal("inner").executes(c -> {
                        IntrinsicConfig cfg = IntrinsicClient.getConfig();
                        cfg.spawnSphereInner = !cfg.spawnSphereInner;
                        cfg.save();
                        reply(c.getSource(), "Inner bubble (24): " + (cfg.spawnSphereInner ? "shown" : "hidden"));
                        return 1;
                    }))
                    .then(ClientCommands.literal("outer").executes(c -> {
                        IntrinsicConfig cfg = IntrinsicClient.getConfig();
                        cfg.spawnSphereOuter = !cfg.spawnSphereOuter;
                        cfg.save();
                        reply(c.getSource(), "Outer shell (128): " + (cfg.spawnSphereOuter ? "shown" : "hidden"));
                        return 1;
                    }))
                    .then(ClientCommands.literal("lock").executes(c -> {
                        LocalPlayer p = Minecraft.getInstance().player;
                        if (p == null) { reply(c.getSource(), "No player to lock to."); return 0; }
                        Vec3 eye = p.getEyePosition(1.0f);
                        lockAt(eye.x, eye.y, eye.z);
                        reply(c.getSource(), String.format("Locked at %.2f / %.2f / %.2f", eye.x, eye.y, eye.z));
                        return 1;
                    }))
                    .then(ClientCommands.literal("unlock").executes(c -> {
                        IntrinsicConfig cfg = IntrinsicClient.getConfig();
                        cfg.spawnSphereLocked = false;
                        cfg.save();
                        reply(c.getSource(), "Unlocked — sphere now follows the player.");
                        return 1;
                    }))
                    .then(ClientCommands.literal("opacity")
                            .then(ClientCommands.argument("value", IntegerArgumentType.integer(0, 255))
                                    .executes(c -> {
                                        int v = IntegerArgumentType.getInteger(c, "value");
                                        IntrinsicConfig cfg = IntrinsicClient.getConfig();
                                        cfg.spawnSphereOpacity = v;
                                        cfg.save();
                                        reply(c.getSource(), "Sphere fill opacity: " + v + "/255");
                                        return 1;
                                    })))
                    .then(ClientCommands.literal("move")
                            .then(ClientCommands.argument("coords", StringArgumentType.greedyString())
                                    .executes(c -> {
                                        LocalPlayer p = Minecraft.getInstance().player;
                                        if (p == null) { reply(c.getSource(), "No player — cannot resolve ~ coords."); return 0; }
                                        Vec3 eye = p.getEyePosition(1.0f);
                                        String raw = StringArgumentType.getString(c, "coords").trim();
                                        String[] parts = raw.split("\\s+");
                                        if (parts.length != 3) {
                                            reply(c.getSource(), "Usage: /ispawnsphere move <x> <y> <z> (supports ~, ~<offset>)");
                                            return 0;
                                        }
                                        try {
                                            double x = parseCoord(parts[0], eye.x);
                                            double y = parseCoord(parts[1], eye.y);
                                            double z = parseCoord(parts[2], eye.z);
                                            lockAt(x, y, z);
                                            reply(c.getSource(), String.format("Moved to %.2f / %.2f / %.2f (locked)", x, y, z));
                                            return 1;
                                        } catch (NumberFormatException nfe) {
                                            reply(c.getSource(), "Invalid coordinate. Use plain numbers or ~ / ~<offset>.");
                                            return 0;
                                        }
                                    }))));
        });
    }

    private static void set(boolean on) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        cfg.spawnSphere = on;
        cfg.save();
    }

    // Mirrors vanilla `~` relative-coord syntax: "~" = base, "~<n>" = base + n,
    // bare number = absolute. Throws NumberFormatException on garbage input.
    private static double parseCoord(String arg, double base) {
        if (arg.isEmpty()) throw new NumberFormatException("empty");
        if (arg.charAt(0) == '~') {
            String rest = arg.substring(1);
            if (rest.isEmpty()) return base;
            return base + Double.parseDouble(rest);
        }
        return Double.parseDouble(arg);
    }

    private static void lockAt(double x, double y, double z) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        cfg.spawnSphereLocked = true;
        cfg.spawnSphereLockX = x;
        cfg.spawnSphereLockY = y;
        cfg.spawnSphereLockZ = z;
        if (!cfg.spawnSphere) cfg.spawnSphere = true;
        cfg.save();
    }

    private static void reply(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource src, String msg) {
        src.sendFeedback(Component.literal("Intrinsic: " + msg));
    }

    private static void drawSphere(VertexConsumer vc, PoseStack.Pose pose, Vec3 center, double radius, int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >>  8) & 0xFF;
        int b =  argb        & 0xFF;
        float cx = (float) center.x;
        float cy = (float) center.y;
        float cz = (float) center.z;
        float rf = (float) radius;
        for (float[] seg : UNIT_SEGMENTS) {
            float ax = cx + seg[0] * rf;
            float ay = cy + seg[1] * rf;
            float az = cz + seg[2] * rf;
            float bx = cx + seg[3] * rf;
            float by = cy + seg[4] * rf;
            float bz = cz + seg[5] * rf;
            float nx = seg[0], ny = seg[1], nz = seg[2];
            vc.addVertex(pose, ax, ay, az).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(1.5f);
            vc.addVertex(pose, bx, by, bz).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(1.5f);
        }
    }

    private static void drawSphereSurface(VertexConsumer vc, PoseStack.Pose pose, Vec3 center,
                                          double radius, int argb, int alphaOverride) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >>  8) & 0xFF;
        int b =  argb        & 0xFF;
        int a = Math.max(0, Math.min(255, alphaOverride));
        float cx = (float) center.x;
        float cy = (float) center.y;
        float cz = (float) center.z;
        float rf = (float) radius;
        for (float[] q : UNIT_QUADS) {
            vc.addVertex(pose, cx + q[0] * rf, cy + q[1]  * rf, cz + q[2]  * rf).setColor(r, g, b, a);
            vc.addVertex(pose, cx + q[3] * rf, cy + q[4]  * rf, cz + q[5]  * rf).setColor(r, g, b, a);
            vc.addVertex(pose, cx + q[6] * rf, cy + q[7]  * rf, cz + q[8]  * rf).setColor(r, g, b, a);
            vc.addVertex(pose, cx + q[9] * rf, cy + q[10] * rf, cz + q[11] * rf).setColor(r, g, b, a);
        }
    }

    // Build filled quad patches on the unit sphere: one quad per (lat, lon) cell,
    // 4 vertices × {x,y,z} = 12 floats per row.
    private static float[][] buildUnitQuads(int lat, int lon) {
        float[][] out = new float[lat * lon][12];
        int idx = 0;
        for (int i = 0; i < lat; i++) {
            double phi1 = Math.PI * ((double) i / lat - 0.5);
            double phi2 = Math.PI * ((double) (i + 1) / lat - 0.5);
            for (int j = 0; j < lon; j++) {
                double th1 = 2.0 * Math.PI * ((double) j / lon);
                double th2 = 2.0 * Math.PI * ((double) (j + 1) / lon);
                float[] quad = out[idx++];
                quad[0]  = (float) (Math.cos(phi1) * Math.cos(th1));
                quad[1]  = (float) Math.sin(phi1);
                quad[2]  = (float) (Math.cos(phi1) * Math.sin(th1));
                quad[3]  = (float) (Math.cos(phi1) * Math.cos(th2));
                quad[4]  = (float) Math.sin(phi1);
                quad[5]  = (float) (Math.cos(phi1) * Math.sin(th2));
                quad[6]  = (float) (Math.cos(phi2) * Math.cos(th2));
                quad[7]  = (float) Math.sin(phi2);
                quad[8]  = (float) (Math.cos(phi2) * Math.sin(th2));
                quad[9]  = (float) (Math.cos(phi2) * Math.cos(th1));
                quad[10] = (float) Math.sin(phi2);
                quad[11] = (float) (Math.cos(phi2) * Math.sin(th1));
            }
        }
        return out;
    }

    // Build latitude + longitude wireframe segments on the unit sphere. Each
    // row in the returned array is {ax, ay, az, bx, by, bz} for one line.
    private static float[][] buildUnitWireframe(int lat, int lon) {
        int segCount = lat * lon + (lat - 1) * lon;
        float[][] out = new float[segCount][6];
        int idx = 0;

        for (int i = 0; i < lat; i++) {
            double phi1 = Math.PI * ((double) i / lat - 0.5);
            double phi2 = Math.PI * ((double) (i + 1) / lat - 0.5);
            for (int j = 0; j < lon; j++) {
                double th = 2.0 * Math.PI * ((double) j / lon);
                float ax = (float) (Math.cos(phi1) * Math.cos(th));
                float ay = (float) Math.sin(phi1);
                float az = (float) (Math.cos(phi1) * Math.sin(th));
                float bx = (float) (Math.cos(phi2) * Math.cos(th));
                float by = (float) Math.sin(phi2);
                float bz = (float) (Math.cos(phi2) * Math.sin(th));
                out[idx++] = new float[]{ax, ay, az, bx, by, bz};
            }
        }

        for (int i = 1; i < lat; i++) {
            double phi = Math.PI * ((double) i / lat - 0.5);
            float y = (float) Math.sin(phi);
            float ring = (float) Math.cos(phi);
            for (int j = 0; j < lon; j++) {
                double th1 = 2.0 * Math.PI * ((double) j / lon);
                double th2 = 2.0 * Math.PI * ((double) (j + 1) / lon);
                float ax = (float) (ring * Math.cos(th1));
                float az = (float) (ring * Math.sin(th1));
                float bx = (float) (ring * Math.cos(th2));
                float bz = (float) (ring * Math.sin(th2));
                out[idx++] = new float[]{ax, y, az, bx, y, bz};
            }
        }

        return out;
    }
}
