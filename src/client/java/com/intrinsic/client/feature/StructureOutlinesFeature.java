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
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Wireframe bounding boxes around nearby structures (strongholds, villages,
// monuments, mansions, fortresses, bastions). Singleplayer-only -- pulls
// StructureStart data from the integrated server's StructureManager. On
// dedicated servers the client has no such data, so the feature silently
// stays off.
public final class StructureOutlinesFeature {
    private static final int CHUNK_RADIUS = 8;
    private static final int REBUILD_INTERVAL_TICKS = 10;

    private record Outline(BoundingBox box, int r, int g, int b) {}

    private static final List<Outline> cache = new ArrayList<>();
    private static int tickCounter = 0;
    private static ResourceKey<Level> cachedDim = null;

    private StructureOutlinesFeature() {}

    public static void register() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(ctx -> {
            if (!Feature.STRUCTURE_OUTLINES.isEnabled()) return;
            Minecraft client = Minecraft.getInstance();
            LocalPlayer player = client.player;
            ClientLevel level = client.level;
            if (player == null || level == null) return;
            if (cache.isEmpty()) return;

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

            for (Outline o : cache) {
                BoundingBox b = o.box();
                drawBoxEdges(vc, pose,
                        b.minX(), b.minY(), b.minZ(),
                        b.maxX() + 1f, b.maxY() + 1f, b.maxZ() + 1f,
                        o.r(), o.g(), o.b(), 0xD0);
            }

            immediate.endBatch(layer);
            matrices.popPose();
        });
    }

    public static void tick(Minecraft client) {
        if (!Feature.STRUCTURE_OUTLINES.isEnabled()) {
            if (!cache.isEmpty()) cache.clear();
            cachedDim = null;
            tickCounter = 0;
            return;
        }
        if (client.player == null || client.level == null) return;

        if (client.level.dimension() != cachedDim) {
            cache.clear();
            cachedDim = client.level.dimension();
            tickCounter = 0;
        }

        tickCounter++;
        if (tickCounter % REBUILD_INTERVAL_TICKS != 0) return;

        rebuild(client);
    }

    private static void rebuild(Minecraft client) {
        cache.clear();

        IntegratedServer server = client.getSingleplayerServer();
        if (server == null) return;

        ServerLevel serverLevel = server.getLevel(client.level.dimension());
        if (serverLevel == null) return;

        Registry<Structure> structureRegistry;
        try {
            structureRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        } catch (Exception ex) {
            return;
        }

        ChunkPos center = client.player.chunkPosition();
        Set<BoundingBox> seen = new HashSet<>();

        for (int cx = center.x() - CHUNK_RADIUS; cx <= center.x() + CHUNK_RADIUS; cx++) {
            for (int cz = center.z() - CHUNK_RADIUS; cz <= center.z() + CHUNK_RADIUS; cz++) {
                List<StructureStart> starts;
                try {
                    starts = serverLevel.structureManager()
                            .startsForStructure(new ChunkPos(cx, cz), s -> true);
                } catch (Exception ex) {
                    continue;
                }
                for (StructureStart start : starts) {
                    if (!start.isValid()) continue;
                    BoundingBox box = start.getBoundingBox();
                    if (!seen.add(box)) continue;

                    Identifier key = structureRegistry.getKey(start.getStructure());
                    int[] rgb = colorFor(key);
                    cache.add(new Outline(box, rgb[0], rgb[1], rgb[2]));
                }
            }
        }
    }

    private static int[] colorFor(Identifier key) {
        if (key == null) return new int[] {0xA0, 0xA0, 0xA0};
        String path = key.getPath();
        // Strongholds -- purple
        if (path.contains("stronghold")) return new int[] {0xA0, 0x60, 0xFF};
        // Villages -- yellow
        if (path.contains("village")) return new int[] {0xFF, 0xE0, 0x60};
        // Monuments / mansions -- cyan
        if (path.contains("monument") || path.contains("mansion")) return new int[] {0x60, 0xE0, 0xFF};
        // Fortresses / bastions -- orange
        if (path.contains("fortress") || path.contains("bastion")) return new int[] {0xFF, 0xA0, 0x40};
        return new int[] {0xA0, 0xA0, 0xA0};
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
