package com.intrinsic.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intrinsic.IntrinsicMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntrinsicConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("intrinsic.json");

    // Master HUD toggle — disables ALL mod HUDs + overlay renders in one click
    public boolean masterHudEnabled = false;

    // HUD Features — all off by default; user opts in from the menu
    public boolean showCoordinates = false;
    public boolean showDurability = false;
    public boolean showFps = false;
    public boolean showClock = false;
    public boolean showDeathCoords = false;
    public boolean showBiome = false;
    public boolean showLightLevel = false;
    public boolean showDayCounter = false;
    public boolean showItemCounter = false;
    public boolean showBlockInfo = false;

    // Gameplay Features
    public boolean zoomEnabled = false;
    public boolean fullbright = false;
    public boolean autoSprint = false;
    public boolean durabilityWarning = false;
    public boolean lowShield = false;
    public boolean lowTotem = false;
    public boolean lowFood = false;
    public boolean lowPotion = false;
    public boolean shulkerPreview = false;
    public boolean fastPlace = false;
    public boolean fastBreak = false;
    public boolean freecamEnabled = false;
    public boolean viewLock = false;
    public boolean smoothCamera = false;
    public double smoothCameraStrength = 0.55;
    public double freecamSpeedMultiplier = 1.0;
    public double freecamMinMult = 0.1;
    public double freecamMaxMult = 10.0;

    // Visual Tweaks
    public boolean noPumpkinOverlay = false;
    public boolean noVignette = false;
    public boolean noFireOverlay = false;
    public boolean hideScoreboard = false;
    public boolean noWeather = false;
    public boolean particlesSuppressed = false;

    // New player HUDs
    public boolean showPotionTimers = false;
    public boolean showEntityInfo = false;
    public boolean armorWarning = false;
    public boolean hungerWarning = false;

    // New player features
    public boolean chatTimestamps = false;
    public boolean chatHighlightsEnabled = false;
    public List<ChatHighlightRule> chatHighlights = new ArrayList<>();
    public boolean inventorySort = false;
    public boolean autoReconnect = false;

    // Technical HUDs
    public boolean chunkBorders = false;
    public boolean slimeChunkIndicator = false;
    public long worldSeed = 0L;
    public boolean signalStrengthDisplay = false;
    public boolean showPing = false;
    public boolean showTps = false;
    public boolean lightHeatmap = false;
    public boolean showHitboxes = false;
    public boolean blockUpdateCounter = false;
    public boolean noFog = false;
    public boolean noLavaFog = false;
    public boolean noWaterFog = false;
    public boolean showVillagerTrades = false;
    public boolean villagerTradesShowPredicted = true;
    public boolean villagerAutoProbe = false;
    public boolean showInsomniaTracker = false;
    public boolean structureOutlines = false;
    public boolean pistonRangePreview = false;

    // Survival HUDs (Task 4)
    public boolean showTotemTracker = false;
    public boolean showSleepTimer = false;
    public boolean showXpProgress = false;
    public boolean showWaypoints = false;
    public boolean showWaypointListHud = true;
    public boolean autoDeathWaypoint = true;

    // Survival gameplay (Task 4)
    public boolean elytraAlert = false;
    public int elytraAlertThreshold = 50;

    // Survival technical (Task 4)
    public boolean mobEsp = false;
    public int mobEspRadius = 32;
    public boolean mobEspThroughWalls = false;
    public boolean beaconRangeOverlay = false;

    // Quality-of-life additions (Task 9)
    public boolean autoEat = false;
    public int autoEatThreshold = 16;
    public boolean cropMaturity = false;
    public int cropMaturityRadius = 24;
    public boolean hotbarTooltip = false;

    // Block content viewer (T4)
    public boolean blockContentViewer = false;
    public int blockContentCacheMinutes = 30;

    // Projectile trajectory preview
    public boolean trajectoryPreview = false;
    public int trajectoryMaxTicks = 100;

    // World stats HUD
    public boolean showWorldStats = false;

    // Spawning sphere overlay
    public boolean spawnSphere = false;
    public boolean spawnSphereInner = true;
    public boolean spawnSphereOuter = true;
    public int spawnSphereInnerColor = 0x80FF4040;
    public int spawnSphereOuterColor = 0x8040FF80;
    // Fill alpha for the translucent sphere surface (0 = invisible, 255 = solid).
    // The wireframe keeps its per-color alpha; this only drives the filled skin.
    public int spawnSphereOpacity = 24;
    // Pinned-center mode. When locked, spheres render at (lockX, lockY, lockZ)
    // rather than following the player — set via /ispawnsphere lock or move.
    public boolean spawnSphereLocked = false;
    public double spawnSphereLockX = 0.0;
    public double spawnSphereLockY = 0.0;
    public double spawnSphereLockZ = 0.0;

    // New QoL features (T5)
    public boolean autoTool = false;
    public boolean autoToolRestorePrevious = true;
    public boolean autoRespawn = false;
    public int autoRespawnDelayMs = 1000;
    public boolean showDeathLog = false;
    public List<DeathEntry> deathLog = new ArrayList<>();
    public boolean mobHealthBar = false;
    public boolean damagePopup = false;
    public boolean autoRefill = false;
    public boolean autoRefillIncludeOffhand = true;
    public boolean autoRefillMatchEnchants = false;

    // T6 features
    public boolean autoTotem = false;
    public double autoTotemThreshold = 6.0;
    public boolean armorSwap = false;
    public boolean quickStash = false;
    public boolean lightLevelNumbers = false;
    public boolean fovOverrideEnabled = false;
    public int fovOverride = 90;

    // Configurable values
    public int durabilityWarningThreshold = 10;
    public double zoomLevel = 4.0;
    public int lightHeatmapRadius = 16;

    // New HUDs (portal converter, bookshelf power, bed warning, low-hp flash)
    public boolean showPortalCoords = false;
    public boolean showBookshelfPower = false;
    public boolean bedWarning = false;
    public boolean lowHpFlash = false;
    public double lowHpFlashThreshold = 0.30;

    // Junk drop — master toggle (keybind still respects this) + blacklist.
    public boolean junkDropEnabled = false;
    public List<String> junkBlacklist = new ArrayList<>(List.of(
            "minecraft:cobblestone",
            "minecraft:cobbled_deepslate",
            "minecraft:dirt",
            "minecraft:granite",
            "minecraft:diorite",
            "minecraft:andesite",
            "minecraft:tuff",
            "minecraft:gravel",
            "minecraft:netherrack",
            "minecraft:rotten_flesh",
            "minecraft:poisonous_potato"
    ));

    // Clean screenshot keybind
    public boolean cleanScreenshot = false;

    // Anti-AFK
    public boolean antiAfk = false;
    public int antiAfkIntervalSeconds = 120;

    // Persisted villager trade cache (see VillagerTradeCache) — keyed by villager UUID string.
    public Map<String, SerializedTrades> villagerTradeCache = new HashMap<>();

    // Waypoint list (persisted)
    public List<Waypoint> waypoints = new ArrayList<>();

    // Built-in texture packs currently enabled through the mod UI.
    public java.util.Set<String> enabledTexturePacks = new java.util.HashSet<>();

    // Per-feature hotkeys (GLFW key code, -1 = unbound)
    public Map<String, Integer> featureKeybinds = new HashMap<>();

    // Customisable HUD positions — keys are "<hudId>.x" / "<hudId>.y"
    // Negative value means offset-from-bottom-or-right.
    public Map<String, Integer> hudPositions = new HashMap<>();

    // Audio tuning
    public boolean audioTuning = false;
    public Map<String, Double> soundVolumes = new HashMap<>();
    public List<CustomAudioPattern> customAudioPatterns = new ArrayList<>();

    public static class SerializedTrades {
        public String professionId;
        public int level;
        public long capturedAtMs;
        public List<SerializedOffer> offers = new ArrayList<>();

        public SerializedTrades() {}
    }

    public static class SerializedOffer {
        public String firstBuyItem;
        public int firstBuyCount;
        public String secondBuyItem;
        public int secondBuyCount;
        public String sellItem;
        public int sellCount;
        public int uses;
        public int maxUses;

        public SerializedOffer() {}
    }

    public static class Waypoint {
        public String name;
        public double x;
        public double y;
        public double z;
        public String dimension;
        public int color = 0xFF55FFFF;
        public boolean pinned = false;

        public Waypoint() {}

        public Waypoint(String name, double x, double y, double z, String dimension, int color) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.dimension = dimension;
            this.color = color;
        }
    }

    public static class DeathEntry {
        public double x;
        public double y;
        public double z;
        public String dimension;
        public long timeMs;

        public DeathEntry() {}

        public DeathEntry(double x, double y, double z, String dimension, long timeMs) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dimension = dimension;
            this.timeMs = timeMs;
        }
    }

    public static class ChatHighlightRule {
        public String pattern;
        public int argb = 0xFFFFD54A;
        public String soundId = "entity.experience_orb.pickup";

        public ChatHighlightRule() {}

        public ChatHighlightRule(String pattern, int argb, String soundId) {
            this.pattern = pattern;
            this.argb = argb;
            this.soundId = soundId;
        }
    }

    public static class CustomAudioPattern {
        public String id;
        public String displayName;
        public String pattern;
        public double volume = 1.0;

        public CustomAudioPattern() {}

        public CustomAudioPattern(String id, String displayName, String pattern, double volume) {
            this.id = id;
            this.displayName = displayName;
            this.pattern = pattern;
            this.volume = volume;
        }
    }

    // Death coordinates (persisted)
    public double lastDeathX = 0;
    public double lastDeathY = 0;
    public double lastDeathZ = 0;
    public String lastDeathDimension = "";
    public boolean hasDeathCoords = false;

    public static IntrinsicConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                IntrinsicConfig config = GSON.fromJson(reader, IntrinsicConfig.class);
                if (config != null) {
                    if (config.featureKeybinds == null) config.featureKeybinds = new HashMap<>();
                    if (config.hudPositions == null) config.hudPositions = new HashMap<>();
                    if (config.soundVolumes == null) config.soundVolumes = new HashMap<>();
                    if (config.customAudioPatterns == null) config.customAudioPatterns = new ArrayList<>();
                    if (config.enabledTexturePacks == null) config.enabledTexturePacks = new java.util.HashSet<>();
                    if (config.villagerTradeCache == null) config.villagerTradeCache = new HashMap<>();
                    if (config.waypoints == null) config.waypoints = new ArrayList<>();
                    if (config.chatHighlights == null) config.chatHighlights = new ArrayList<>();
                    return config;
                }
            } catch (Exception e) {
                IntrinsicMod.LOGGER.error("Failed to load config, using defaults", e);
            }
        }
        IntrinsicConfig config = new IntrinsicConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            IntrinsicMod.LOGGER.error("Failed to save config", e);
        }
    }
}
