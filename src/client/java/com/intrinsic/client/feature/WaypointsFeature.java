package com.intrinsic.client.feature;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Iterator;
import java.util.List;

public final class WaypointsFeature {
    private WaypointsFeature() {}

    public static void register() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(ctx -> {
            if (!Feature.WAYPOINTS.isEnabled()) return;
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.level == null) return;

            LevelRenderState state = ctx.levelState();
            if (state == null) return;
            CameraRenderState cam = state.cameraRenderState;
            if (cam == null || !cam.initialized || cam.pos == null) return;
            Vec3 camPos = cam.pos;

            String dim = client.level.dimension().identifier().toString();
            IntrinsicConfig cfg = IntrinsicClient.getConfig();
            if (cfg.waypoints.isEmpty()) return;

            PoseStack matrices = new PoseStack();
            matrices.pushPose();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
            Matrix4f mat = matrices.last().pose();

            MultiBufferSource.BufferSource immediate =
                    client.renderBuffers().bufferSource();
            VertexConsumer vc = immediate.getBuffer(RenderTypes.debugQuads());

            for (IntrinsicConfig.Waypoint wp : cfg.waypoints) {
                if (wp.dimension != null && !wp.dimension.isEmpty() && !wp.dimension.equals(dim)) continue;
                drawBeam(vc, mat, (float) wp.x, (float) wp.z, wp.color);
            }

            immediate.endBatch(RenderTypes.debugQuads());
            matrices.popPose();
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("iwaypoint")
                    .then(ClientCommands.literal("add")
                            .then(ClientCommands.argument("name", StringArgumentType.greedyString())
                                    .executes(c -> {
                                        String name = StringArgumentType.getString(c, "name");
                                        LocalPlayer p = Minecraft.getInstance().player;
                                        if (p == null) return 0;
                                        return addWaypoint(c, name, p.getX(), p.getY(), p.getZ(), 0xFF55FFFF);
                                    })))
                    .then(ClientCommands.literal("remove")
                            .then(ClientCommands.argument("name", StringArgumentType.greedyString())
                                    .executes(c -> {
                                        String name = StringArgumentType.getString(c, "name");
                                        return removeWaypoint(c, name);
                                    })))
                    .then(ClientCommands.literal("color")
                            .then(ClientCommands.argument("name", StringArgumentType.string())
                                    .then(ClientCommands.argument("argb", IntegerArgumentType.integer())
                                            .executes(c -> {
                                                String name = StringArgumentType.getString(c, "name");
                                                int color = IntegerArgumentType.getInteger(c, "argb");
                                                return recolorWaypoint(c, name, color);
                                            }))))
                    .then(ClientCommands.literal("list")
                            .executes(c -> listWaypoints(c)))
                    .then(ClientCommands.literal("hud")
                            .then(ClientCommands.literal("on").executes(c -> setHud(c, true)))
                            .then(ClientCommands.literal("off").executes(c -> setHud(c, false))))
                    .executes(c -> listWaypoints(c)));
        });
    }

    private static int addWaypoint(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> c,
                                    String name, double x, double y, double z, int color) {
        Minecraft client = Minecraft.getInstance();
        String dim = client.level != null ? client.level.dimension().identifier().toString() : "";
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        cfg.waypoints.add(new IntrinsicConfig.Waypoint(name, x, y, z, dim, color));
        cfg.save();
        c.getSource().sendFeedback(Component.literal("Waypoint '" + name + "' added at "
                + (int) x + "," + (int) y + "," + (int) z).withStyle(ChatFormatting.AQUA));
        return 1;
    }

    private static int removeWaypoint(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> c,
                                       String name) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        Iterator<IntrinsicConfig.Waypoint> it = cfg.waypoints.iterator();
        boolean removed = false;
        while (it.hasNext()) {
            IntrinsicConfig.Waypoint wp = it.next();
            if (name.equalsIgnoreCase(wp.name)) {
                it.remove();
                removed = true;
            }
        }
        cfg.save();
        c.getSource().sendFeedback(Component.literal(removed
                ? "Waypoint '" + name + "' removed."
                : "No waypoint named '" + name + "'.").withStyle(removed ? ChatFormatting.YELLOW : ChatFormatting.RED));
        return removed ? 1 : 0;
    }

    private static int recolorWaypoint(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> c,
                                        String name, int color) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        for (IntrinsicConfig.Waypoint wp : cfg.waypoints) {
            if (name.equalsIgnoreCase(wp.name)) {
                wp.color = color;
                cfg.save();
                c.getSource().sendFeedback(Component.literal("Recolored '" + name + "'.").withStyle(ChatFormatting.AQUA));
                return 1;
            }
        }
        c.getSource().sendFeedback(Component.literal("No waypoint named '" + name + "'.").withStyle(ChatFormatting.RED));
        return 0;
    }

    private static int setHud(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> c,
                              boolean on) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        cfg.showWaypointListHud = on;
        cfg.save();
        c.getSource().sendFeedback(Component.literal("Waypoint HUD list " + (on ? "enabled." : "hidden (beams still render)."))
                .withStyle(on ? ChatFormatting.AQUA : ChatFormatting.GRAY));
        return 1;
    }

    private static int listWaypoints(com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> c) {
        List<IntrinsicConfig.Waypoint> list = IntrinsicClient.getConfig().waypoints;
        if (list.isEmpty()) {
            c.getSource().sendFeedback(Component.literal("No waypoints. Use /iwaypoint add <name>.").withStyle(ChatFormatting.GRAY));
            return 0;
        }
        c.getSource().sendFeedback(Component.literal("Intrinsic waypoints (" + list.size() + "):").withStyle(ChatFormatting.AQUA));
        for (IntrinsicConfig.Waypoint wp : list) {
            c.getSource().sendFeedback(Component.literal(" - " + wp.name + " ("
                    + (int) wp.x + "," + (int) wp.y + "," + (int) wp.z + ")")
                    .withStyle(ChatFormatting.GRAY));
        }
        return 1;
    }

    private static void drawBeam(VertexConsumer vc, Matrix4f mat, float cx, float cz, int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int a = 80;
        float x0 = cx + 0.3f, x1 = cx + 0.7f;
        float z0 = cz + 0.3f, z1 = cz + 0.7f;
        float y0 = -64f, y1 = 320f;

        vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);

        vc.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);

        vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
        vc.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);

        vc.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);
    }

    public static void onPlayerDeath(double x, double y, double z, String dim) {
        if (!IntrinsicClient.getConfig().autoDeathWaypoint) return;
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        cfg.waypoints.removeIf(w -> "Death".equals(w.name));
        cfg.waypoints.add(new IntrinsicConfig.Waypoint("Death", x, y, z, dim, 0xFFFF5555));
        cfg.save();
    }
}
