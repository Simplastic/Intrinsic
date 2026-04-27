package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import com.intrinsic.client.feature.Feature;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HudPositions {
    public enum Origin {
        TL, TC, TR,
        ML, MC, MR,
        BL, BC, BR
    }

    public static final class Anchor {
        public final String displayName;
        public final Origin origin;
        public final int defaultOffsetX;
        public final int defaultOffsetY;
        public final int width;
        public final int height;

        public Anchor(String displayName, Origin origin, int dx, int dy, int w, int h) {
            this.displayName = displayName;
            this.origin = origin;
            this.defaultOffsetX = dx;
            this.defaultOffsetY = dy;
            this.width = w;
            this.height = h;
        }
    }

    private static final Map<String, Anchor> ANCHORS = new LinkedHashMap<>();
    private static final Map<String, Feature> HUD_TO_FEATURE = new HashMap<>();
    static {
        HUD_TO_FEATURE.put("coordinates",       Feature.COORDINATES);
        HUD_TO_FEATURE.put("biome",             Feature.BIOME);
        HUD_TO_FEATURE.put("lightLevel",        Feature.LIGHT_LEVEL);
        HUD_TO_FEATURE.put("fps",               Feature.FPS);
        HUD_TO_FEATURE.put("clock",             Feature.CLOCK);
        HUD_TO_FEATURE.put("dayCounter",        Feature.DAY_COUNTER);
        HUD_TO_FEATURE.put("ping",              Feature.PING);
        HUD_TO_FEATURE.put("tps",               Feature.TPS);
        HUD_TO_FEATURE.put("chunkBorders",      Feature.CHUNK_BORDERS);
        HUD_TO_FEATURE.put("slimeChunkMap",     Feature.SLIME_CHUNKS);
        HUD_TO_FEATURE.put("durability",        Feature.DURABILITY_HUD);
        HUD_TO_FEATURE.put("deathCoords",       Feature.DEATH_COORDS);
        HUD_TO_FEATURE.put("blockUpdate",       Feature.BLOCK_UPDATE_COUNTER);
        HUD_TO_FEATURE.put("villagerTrades",    Feature.VILLAGER_TRADES);
        HUD_TO_FEATURE.put("potionEffects",     Feature.POTION_TIMERS);
        HUD_TO_FEATURE.put("itemCounter",       Feature.ITEM_COUNTER);
        HUD_TO_FEATURE.put("blockInfo",         Feature.BLOCK_INFO);
        HUD_TO_FEATURE.put("entityInfo",        Feature.ENTITY_INFO);
        HUD_TO_FEATURE.put("signalStrength",    Feature.SIGNAL_STRENGTH);
        HUD_TO_FEATURE.put("durabilityWarning", Feature.DURABILITY_WARNING);
        HUD_TO_FEATURE.put("armorWarning",      Feature.ARMOR_WARNING);
        HUD_TO_FEATURE.put("hungerWarning",     Feature.HUNGER_WARNING);
        HUD_TO_FEATURE.put("totemTracker",      Feature.TOTEM_TRACKER);
        HUD_TO_FEATURE.put("sleepTimer",        Feature.SLEEP_TIMER);
        HUD_TO_FEATURE.put("xpProgress",        Feature.XP_PROGRESS);
        HUD_TO_FEATURE.put("waypointList",      Feature.WAYPOINTS);
        HUD_TO_FEATURE.put("elytraAlert",       Feature.ELYTRA_ALERT);
        HUD_TO_FEATURE.put("insomniaTracker",   Feature.INSOMNIA_TRACKER);
        HUD_TO_FEATURE.put("hotbarTooltip",     Feature.HOTBAR_TOOLTIP);
        HUD_TO_FEATURE.put("blockContent",      Feature.BLOCK_CONTENT_VIEWER);
        HUD_TO_FEATURE.put("deathLog",          Feature.DEATH_LOG);
        HUD_TO_FEATURE.put("mobHealthBar",      Feature.MOB_HEALTH_BAR);
        HUD_TO_FEATURE.put("worldStats",        Feature.WORLD_STATS);
        HUD_TO_FEATURE.put("portalCoords",      Feature.PORTAL_COORDS);
        HUD_TO_FEATURE.put("bookshelfPower",    Feature.BOOKSHELF_POWER);
        HUD_TO_FEATURE.put("bedWarning",        Feature.BED_WARNING);
        // toggleToast has no backing Feature (UI-only) — intentionally not mapped,
        // which hides it from the repositionable list.
    }

    static {
        register("coordinates",       "Coordinates",        Origin.TL,   4,   4, 160, 10);
        register("biome",             "Biome",              Origin.TL,   4,  16, 140, 10);
        register("lightLevel",        "Light Level HUD",    Origin.TL,   4,  28, 180, 10);
        register("fps",               "FPS",                Origin.TR,  -4,   4,  60, 10);
        register("clock",             "Clock",              Origin.TR,  -4,  16,  80, 10);
        register("dayCounter",        "Day Counter",        Origin.TR,  -4,  28,  60, 10);
        register("ping",              "Ping",               Origin.TR,  -4,  40,  60, 10);
        register("tps",               "TPS / MSPT",         Origin.TR,  -4,  52, 120, 10);
        register("chunkBorders",      "Chunk Border Map",   Origin.TR,  -6,   6,  64, 80);
        register("slimeChunkMap",     "Slime Chunk Map",    Origin.TR,  -6,  96,  64, 64);
        register("durability",        "Durability List",    Origin.BR,  -4,  -4, 120, 60);
        register("deathCoords",       "Death Coords",       Origin.BL,   4,  -4, 220, 10);
        register("blockUpdate",       "Block Updates",      Origin.BL,   4, -16, 160, 10);
        register("villagerTrades",    "Villager Trades",    Origin.TR,  -4,  80, 240, 180);
        register("potionEffects",     "Potion Effects",     Origin.BL,   4, -52, 160, 80);
        register("itemCounter",       "Item Counter",       Origin.BC,  97, -14,  58, 16);
        register("blockInfo",         "Block Info",         Origin.BC,   0, -130, 220, 60);
        register("toggleToast",       "Toggle Toast",       Origin.BC,   0, -88, 200, 14);
        register("entityInfo",        "Entity Info",        Origin.MC,   0, -60, 220, 70);
        register("signalStrength",    "Signal Strength",    Origin.MC,   0,  12, 220, 100);
        register("durabilityWarning", "Durability Warning", Origin.MC,   0,  20, 200, 14);
        register("armorWarning",      "Armor Warning",      Origin.MC,   0,  34, 200, 10);
        register("hungerWarning",     "Hunger Warning",     Origin.MC,   0,  48, 160, 10);
        register("totemTracker",      "Totem Tracker",      Origin.ML,   4, -30,  80, 20);
        register("sleepTimer",        "Sleep Timer",        Origin.TC,   0,  60, 140, 14);
        register("xpProgress",        "XP Progress",        Origin.BC,   0, -30, 140, 12);
        register("waypointList",      "Waypoint List",      Origin.MR,  -4,   0, 140, 60);
        register("elytraAlert",       "Elytra Alert",       Origin.MC,   0,  62, 200, 14);
        register("insomniaTracker",   "Insomnia Tracker",   Origin.TR,  -4, 170, 170, 14);
        register("hotbarTooltip",     "Hotbar Tooltip",     Origin.BC,   0, -52, 240, 60);
        register("blockContent",      "Block Content Viewer", Origin.BL,   4, -170, 180, 120);
        register("deathLog",          "Death Log",          Origin.BL,   4, -20, 240, 60);
        register("mobHealthBar",      "Mob Health Bar",     Origin.TC,   0,  30, 200, 16);
        register("worldStats",        "World Stats",        Origin.TR,  -4, 190, 140, 46);
        register("portalCoords",      "Portal Coords",      Origin.BC,   0, -160, 180, 40);
        register("bookshelfPower",    "Bookshelf Power",    Origin.MC,   0, -30, 160, 22);
        register("bedWarning",        "Bed Warning",        Origin.TC,   0,  80, 220, 14);
    }

    private static void register(String id, String name, Origin origin, int dx, int dy, int w, int h) {
        ANCHORS.put(id, new Anchor(name, origin, dx, dy, w, h));
    }

    public static Map<String, Anchor> all() {
        return ANCHORS;
    }

    public static Anchor get(String id) {
        return ANCHORS.get(id);
    }

    public static int x(String id, int screenWidth) {
        Anchor a = ANCHORS.get(id);
        if (a == null) return 0;
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        Integer saved = cfg.hudPositions.get(id + ".x");
        if (saved != null) return saved;
        return defaultX(a, screenWidth);
    }

    public static int y(String id, int screenHeight) {
        Anchor a = ANCHORS.get(id);
        if (a == null) return 0;
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        Integer saved = cfg.hudPositions.get(id + ".y");
        if (saved != null) return saved;
        return defaultY(a, screenHeight);
    }

    public static int defaultX(Anchor a, int sw) {
        return switch (a.origin) {
            case TL, ML, BL -> a.defaultOffsetX;
            case TC, MC, BC -> sw / 2 - a.width / 2 + a.defaultOffsetX;
            case TR, MR, BR -> sw - a.width + a.defaultOffsetX;
        };
    }

    public static int defaultY(Anchor a, int sh) {
        return switch (a.origin) {
            case TL, TC, TR -> a.defaultOffsetY;
            case ML, MC, MR -> sh / 2 - a.height / 2 + a.defaultOffsetY;
            case BL, BC, BR -> sh - a.height + a.defaultOffsetY;
        };
    }

    public static void setPosition(String id, int x, int y) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        cfg.hudPositions.put(id + ".x", x);
        cfg.hudPositions.put(id + ".y", y);
    }

    public static void reset(String id) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        cfg.hudPositions.remove(id + ".x");
        cfg.hudPositions.remove(id + ".y");
    }

    public static boolean isOverridden(String id) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        return cfg.hudPositions.containsKey(id + ".x") || cfg.hudPositions.containsKey(id + ".y");
    }

    public static Feature featureFor(String hudId) {
        return HUD_TO_FEATURE.get(hudId);
    }

    public static boolean isHudActive(String hudId) {
        Feature f = HUD_TO_FEATURE.get(hudId);
        return f != null && f.isEnabled();
    }
}
