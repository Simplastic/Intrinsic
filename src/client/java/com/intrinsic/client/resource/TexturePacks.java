package com.intrinsic.client.resource;

import com.intrinsic.IntrinsicMod;
import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TexturePacks {

    public record Pack(String id, String displayName, String description) {
        public String fabricPackName() {
            return "intrinsic/" + id;
        }

        public Identifier thumbnail() {
            return Identifier.fromNamespaceAndPath("intrinsic", "textures/gui/packs/" + id + ".png");
        }
    }

    public static final List<Pack> ALL = List.of(
            new Pack("vt_3d_everything", "3D Everything", "Adds a bunch of 3D models into the game."),
            new Pack("vt_brewing_guide", "Brewing Guide", "Adds a visual indicator for brewing recipes."),
            new Pack("vt_bushier_leaves", "Bushier Leaves", "The leaves blocks are now bushier."),
            new Pack("vt_circular_sun_moon", "Circular Sun & Moon", "Replace the Sun and Moon models to be round."),
            new Pack("vt_dark_ui", "Dark UI", "Dark theme for the Vanilla UI."),
            new Pack("vt_full_grass", "Full Grass Blocks & Such", "Grass blocks & such are now full of grass on every side."),
            new Pack("vt_golden_savanna", "Golden Savanna", "Golden grass for savanna biomes."),
            new Pack("vt_lower_grass", "Lower Grass Blocks & Such", "The grass blocks & such tecture is now lower on the sides."),
            new Pack("vt_lush_grass", "Lush Grass", "Lushier textures for most biomes."),
            new Pack("vt_precious_blocks_border", "Precious Blocks Border", "Adds a border on precious blocks (ore, suspicious blocks, amethyst buds)."),
            new Pack("vt_redstone_utility", "Redstone Utility", "Visual indicators for nearly every redstone blocks."),
            new Pack("vt_stems_kelp_helpers", "Stems & Kelp Helpers", "Age 25 kelp and pumpkin/melon stems."),
            new Pack("vt_twinkling_stars", "Twinkling Stars", "Twinkling stars during the night."),
            new Pack("vt_unique_axolotl_painting", "Unique Axolotl & Painting Items", "Axolotl buckets and paintings textures."),
            new Pack("vt_unobstrusive_rain_snow", "Unobstrusive Rain & Snow", "Unobstrusive rain and snow textures for better visibility."),
            new Pack("vt_variated_blocks", "Variated Blocks", "Adds various textures to plenty of blocks."),
            new Pack("vt_visual_infested_waxed", "Visual Infested & Waxed Blocks", "Visual indicators for infested and waxed blocks items only.")
    );

    private static boolean registered = false;
    private static boolean initialSyncDone = false;
    private static Set<String> snapshotAtOpen = null;

    private TexturePacks() {}

    public static void registerAll() {
        if (registered) return;
        ModContainer container = FabricLoader.getInstance().getModContainer("intrinsic").orElse(null);
        if (container == null) {
            IntrinsicMod.LOGGER.warn("Cannot register texture packs: mod container not found");
            return;
        }
        for (Pack p : ALL) {
            boolean ok = ResourceManagerHelper.registerBuiltinResourcePack(
                    Identifier.fromNamespaceAndPath("intrinsic", p.id),
                    container,
                    Component.literal(p.displayName),
                    ResourcePackActivationType.NORMAL);
            if (!ok) {
                IntrinsicMod.LOGGER.warn("Failed to register texture pack {}", p.id);
            }
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (initialSyncDone) return;
            if (client.getResourcePackRepository() == null) return;
            initialSyncDone = true;
            IntrinsicConfig cfg = IntrinsicClient.getConfig();
            if (cfg == null || cfg.enabledTexturePacks.isEmpty() || ALL.isEmpty()) return;
            if (syncSelection(client)) {
                client.reloadResourcePacks();
            }
        });
        registered = true;
    }

    public static boolean isEnabled(String id) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        return cfg.enabledTexturePacks.contains(id);
    }

    public static void setEnabled(String id, boolean on) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        boolean changed = on ? cfg.enabledTexturePacks.add(id) : cfg.enabledTexturePacks.remove(id);
        if (!changed) return;
        cfg.save();
    }

    public static void captureSnapshot() {
        snapshotAtOpen = new HashSet<>(IntrinsicClient.getConfig().enabledTexturePacks);
    }

    public static boolean isDirty() {
        if (snapshotAtOpen == null) return false;
        return !snapshotAtOpen.equals(IntrinsicClient.getConfig().enabledTexturePacks);
    }

    public static void applyNow(Minecraft client) {
        if (client == null) return;
        boolean changed = syncSelection(client);
        if (changed) {
            client.reloadResourcePacks();
        }
        snapshotAtOpen = new HashSet<>(IntrinsicClient.getConfig().enabledTexturePacks);
    }

    private static String resolveProfileId(net.minecraft.server.packs.repository.PackRepository mgr, String packId) {
        String[] candidates = {
                "intrinsic:" + packId,
                "intrinsic/" + packId,
                "mod_resources:intrinsic:" + packId,
                "file/intrinsic:" + packId,
                "builtin/" + packId,
                packId
        };
        var profiles = mgr.getAvailablePacks();
        var availableIds = new HashSet<String>();
        for (var prof : profiles) availableIds.add(prof.getId());
        for (String c : candidates) {
            if (availableIds.contains(c)) return c;
        }
        for (String id : availableIds) {
            if (id.contains(packId) && id.toLowerCase().contains("intrinsic")) return id;
        }
        return null;
    }

    private static boolean loggedProfiles = false;

    private static boolean syncSelection(Minecraft client) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        var mgr = client.getResourcePackRepository();
        mgr.reload();

        if (!loggedProfiles) {
            loggedProfiles = true;
            IntrinsicMod.LOGGER.info("[TexturePacks] Available resource pack profiles:");
            for (var prof : mgr.getAvailablePacks()) {
                IntrinsicMod.LOGGER.info("  - {}", prof.getId());
            }
        }

        var before = new java.util.ArrayList<>(mgr.getSelectedIds());
        var enabled = new java.util.ArrayList<>(before);
        for (Pack p : ALL) {
            String profileId = resolveProfileId(mgr, p.id);
            boolean shouldBeOn = cfg.enabledTexturePacks.contains(p.id);
            if (profileId == null) {
                if (shouldBeOn) {
                    IntrinsicMod.LOGGER.warn("[TexturePacks] Profile not found for pack '{}' — pack won't apply", p.id);
                }
                continue;
            }
            boolean currentlyOn = enabled.contains(profileId);
            if (shouldBeOn && !currentlyOn) enabled.add(profileId);
            if (!shouldBeOn && currentlyOn) enabled.remove(profileId);
        }
        if (enabled.equals(before)) return false;
        mgr.setSelected(enabled);
        return true;
    }
}
