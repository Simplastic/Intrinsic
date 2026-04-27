package com.intrinsic.client.feature;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;

public enum Feature {
    // HUD
    MASTER_HUD("master_hud", "Master HUD Toggle", Category.HUD, "Disable every mod HUD in one click.", c -> c.masterHudEnabled, (c, v) -> c.masterHudEnabled = v),
    COORDINATES("coordinates", "Coordinates HUD", Category.HUD, "Show X / Y / Z and facing direction. Repositionable via HUD Layout.", c -> c.showCoordinates, (c, v) -> c.showCoordinates = v),
    DURABILITY_HUD("durability_hud", "Durability HUD", Category.HUD, "Compact durability bars for armor and tools. Repositionable via HUD Layout.", c -> c.showDurability, (c, v) -> c.showDurability = v),
    FPS("fps", "FPS Counter", Category.HUD, "Live frames per second. Repositionable via HUD Layout.", c -> c.showFps, (c, v) -> c.showFps = v),
    CLOCK("clock", "In-Game Clock", Category.HUD, "Day-night cycle clock. Repositionable via HUD Layout.", c -> c.showClock, (c, v) -> c.showClock = v),
    DEATH_COORDS("death_coords", "Death Coordinates", Category.HUD, "Remember where you last died. Repositionable via HUD Layout.", c -> c.showDeathCoords, (c, v) -> c.showDeathCoords = v),
    BIOME("biome", "Biome Display", Category.HUD, "Current biome name. Repositionable via HUD Layout.", c -> c.showBiome, (c, v) -> c.showBiome = v),
    LIGHT_LEVEL("light_level", "Light Level HUD", Category.HUD, "Light level at your feet. Repositionable via HUD Layout.", c -> c.showLightLevel, (c, v) -> c.showLightLevel = v),
    DAY_COUNTER("day_counter", "Day Counter", Category.HUD, "Days since the world was created. Repositionable via HUD Layout.", c -> c.showDayCounter, (c, v) -> c.showDayCounter = v),
    ITEM_COUNTER("item_counter", "Item Counter", Category.HUD, "Count of the item you hold. Repositionable via HUD Layout.", c -> c.showItemCounter, (c, v) -> c.showItemCounter = v),
    BLOCK_INFO("block_info", "Block Info HUD", Category.HUD, "Show contextual info on the block you look at (bee hives, composters, furnaces, crops). Repositionable via HUD Layout.", c -> c.showBlockInfo, (c, v) -> c.showBlockInfo = v),
    POTION_TIMERS("potion_timers", "Potion Timers", Category.HUD, "Active status effect timers. Repositionable via HUD Layout.", c -> c.showPotionTimers, (c, v) -> c.showPotionTimers = v),
    ENTITY_INFO("entity_info", "Entity Info", Category.HUD, "Info about the entity you look at. Repositionable via HUD Layout.", c -> c.showEntityInfo, (c, v) -> c.showEntityInfo = v),
    ARMOR_WARNING("armor_warning", "Armor Warning", Category.HUD, "Warn when a piece is nearly broken. Repositionable via HUD Layout.", c -> c.armorWarning, (c, v) -> c.armorWarning = v),
    HUNGER_WARNING("hunger_warning", "Hunger Warning", Category.HUD, "Warn when hunger gets low. Repositionable via HUD Layout.", c -> c.hungerWarning, (c, v) -> c.hungerWarning = v),
    TOTEM_TRACKER("totem_tracker", "Totem Tracker", Category.HUD, "Count totems of undying across your inventory. Repositionable via HUD Layout.", c -> c.showTotemTracker, (c, v) -> c.showTotemTracker = v),
    SLEEP_TIMER("sleep_timer", "Sleep Timer", Category.HUD, "Countdown until dawn while sleeping, or danger scan at night. Repositionable via HUD Layout.", c -> c.showSleepTimer, (c, v) -> c.showSleepTimer = v),
    XP_PROGRESS("xp_progress", "XP Progress", Category.HUD, "Current level + points to the next level. Repositionable via HUD Layout.", c -> c.showXpProgress, (c, v) -> c.showXpProgress = v),
    WAYPOINTS("waypoints", "Waypoints", Category.HUD, "Beam markers + list of saved coordinates. Manage via /iwaypoint. Repositionable via HUD Layout.", c -> c.showWaypoints, (c, v) -> c.showWaypoints = v),
    INSOMNIA_TRACKER("insomnia_tracker", "Insomnia Tracker", Category.HUD, "Countdown before phantoms can spawn at night. Repositionable via HUD Layout.", c -> c.showInsomniaTracker, (c, v) -> c.showInsomniaTracker = v),
    HOTBAR_TOOLTIP("hotbar_tooltip", "Hotbar Tooltip", Category.HUD, "Item name, enchantments and durability over the hotbar (for held items and items you aim at). Replaces the vanilla selected-item popup. Repositionable via HUD Layout.", c -> c.hotbarTooltip, (c, v) -> c.hotbarTooltip = v),
    BLOCK_CONTENT_VIEWER("block_content_viewer", "Block Content Viewer", Category.HUD, "Remember the last contents of chests/barrels/shulkers/hoppers/dispensers/brewing stands you open. Repositionable via HUD Layout.", c -> c.blockContentViewer, (c, v) -> c.blockContentViewer = v),
    DEATH_LOG("death_log", "Death Log", Category.HUD, "List of your last 5 deaths with timestamps. Repositionable via HUD Layout.", c -> c.showDeathLog, (c, v) -> c.showDeathLog = v),
    MOB_HEALTH_BAR("mob_health_bar", "Mob Health Bar", Category.HUD, "Health bar above the crosshair for the living entity you look at. Repositionable via HUD Layout.", c -> c.mobHealthBar, (c, v) -> c.mobHealthBar = v),
    DAMAGE_POPUP("damage_popup", "Damage Popup", Category.HUD, "Show floating red damage numbers above living entities when they take damage. Client-side delta tracking \u2014 amount reflects the server-synced HP drop.", c -> c.damagePopup, (c, v) -> c.damagePopup = v),
    PORTAL_COORDS("portal_coords", "Portal Coords HUD", Category.HUD, "Convert X/Z between Overworld and Nether when looking at a portal. Repositionable via HUD Layout.", c -> c.showPortalCoords, (c, v) -> c.showPortalCoords = v),
    BOOKSHELF_POWER("bookshelf_power", "Bookshelf Power", Category.HUD, "Count of valid bookshelves powering the enchanting table you look at. Repositionable via HUD Layout.", c -> c.showBookshelfPower, (c, v) -> c.showBookshelfPower = v),
    BED_WARNING("bed_warning", "Bed Explosion Warning", Category.HUD, "Warn when looking at a bed in the Nether or End (it explodes). Repositionable via HUD Layout.", c -> c.bedWarning, (c, v) -> c.bedWarning = v),
    LOW_HP_FLASH("low_hp_flash", "Low HP Flash", Category.HUD, "Pulsing red screen vignette when your health drops below the threshold.", c -> c.lowHpFlash, (c, v) -> c.lowHpFlash = v),

    // Gameplay
    ZOOM("zoom", "Zoom", Category.GAMEPLAY, "Hold the keybind to zoom the camera.", Kind.HOLD, c -> c.zoomEnabled, (c, v) -> c.zoomEnabled = v),
    FULLBRIGHT("fullbright", "Fullbright", Category.GAMEPLAY, "Max gamma, always see in the dark.", c -> c.fullbright, (c, v) -> c.fullbright = v),
    SPRINT_TOGGLE("sprint_toggle", "Sprint Toggle", Category.GAMEPLAY, "Toggle sprint instead of holding.", c -> c.autoSprint, (c, v) -> c.autoSprint = v),
    DURABILITY_WARNING("durability_warning", "Durability Warning", Category.GAMEPLAY, "Popup before a tool breaks. Repositionable via HUD Layout.", c -> c.durabilityWarning, (c, v) -> c.durabilityWarning = v),
    LOW_SHIELD("low_shield", "Low Shield", Category.GAMEPLAY, "Lower the shield in view when not blocking.", c -> c.lowShield, (c, v) -> c.lowShield = v),
    LOW_TOTEM("low_totem", "Low Totem", Category.GAMEPLAY, "Lower a held totem so it does not block the view.", c -> c.lowTotem, (c, v) -> c.lowTotem = v),
    LOW_FOOD("low_food", "Low Food", Category.GAMEPLAY, "Lower held food items when not eating.", c -> c.lowFood, (c, v) -> c.lowFood = v),
    LOW_POTION("low_potion", "Low Potion", Category.GAMEPLAY, "Lower held potions (regular, splash, lingering).", c -> c.lowPotion, (c, v) -> c.lowPotion = v),
    SHULKER_PREVIEW("shulker_preview", "Shulker Preview", Category.GAMEPLAY, "Hover a shulker to peek inside.", c -> c.shulkerPreview, (c, v) -> c.shulkerPreview = v),
    CHAT_TIMESTAMPS("chat_timestamps", "Chat Timestamps", Category.GAMEPLAY, "Prefix chat messages with the time.", c -> c.chatTimestamps, (c, v) -> c.chatTimestamps = v),
    CHAT_HIGHLIGHTS("chat_highlights", "Chat Highlights", Category.GAMEPLAY, "Highlight matching regex patterns in chat and play a ping sound. A rule for your own username is added automatically on first run; edit the full list in config/intrinsic.json (chatHighlights).", c -> c.chatHighlightsEnabled, (c, v) -> c.chatHighlightsEnabled = v),
    INVENTORY_SORT("inventory_sort", "Inventory Sort", Category.GAMEPLAY, "Press the sort key in any inventory (disabled in the creative menu). Works on the player inventory, chests, barrels, ender/trapped chests and shulkers.", Kind.TRIGGER, c -> c.inventorySort, (c, v) -> c.inventorySort = v),
    AUTO_RECONNECT("auto_reconnect", "Auto Reconnect", Category.GAMEPLAY, "One-click reconnect on disconnect.", c -> c.autoReconnect, (c, v) -> c.autoReconnect = v),
    AUDIO_TUNING("audio_tuning", "Audio Tuning", Category.GAMEPLAY, "Attenuate noisy sounds (redstone, weather, mobs) from the Audio tab.", c -> c.audioTuning, (c, v) -> c.audioTuning = v),
    FAST_PLACE("fast_place", "Fast Place", Category.GAMEPLAY, "Remove the right-click placement cooldown.", c -> c.fastPlace, (c, v) -> c.fastPlace = v),
    FAST_BREAK("fast_break", "Fast Break", Category.GAMEPLAY, "Remove the break cooldown between blocks (creative only).", c -> c.fastBreak, (c, v) -> c.fastBreak = v),
    FREECAM("freecam", "Freecam", Category.GAMEPLAY, "Detach the camera from your player. The camera roams freely through blocks while your body keeps its normal physics (gravity, inertia, elytra) \u2014 no position freeze, safe on anti-cheat servers. Inputs are routed to the camera, so the body decays to rest. Bind a key in Keybinds. Range is capped by the chunks loaded around your body.", c -> c.freecamEnabled, (c, v) -> c.freecamEnabled = v),
    VIEW_LOCK("view_lock", "View Lock", Category.GAMEPLAY, "Freeze the camera yaw and pitch at the current angle. Feature toggle gates the keybind; press the bound key in-world to lock/unlock the current angle. Useful for AFK screenshots and steady-cam recording.", Kind.TRIGGER, c -> c.viewLock, (c, v) -> c.viewLock = v),
    SMOOTH_CAMERA("smooth_camera", "Smooth Camera", Category.GAMEPLAY, "Exponentially smooth the raw mouse delta for a cinematic feel. Adjust strength via smoothCameraStrength in config (0.0 \u2013 0.95).", c -> c.smoothCamera, (c, v) -> c.smoothCamera = v),
    ELYTRA_ALERT("elytra_alert", "Elytra Alert", Category.GAMEPLAY, "Warn when your worn elytra is close to breaking.", c -> c.elytraAlert, (c, v) -> c.elytraAlert = v),
    AUTO_EAT("auto_eat", "Auto-Eat", Category.GAMEPLAY, "Automatically eat food from the hotbar when hunger drops below the threshold.", c -> c.autoEat, (c, v) -> c.autoEat = v),
    AUTO_TOOL("auto_tool", "Auto Tool", Category.GAMEPLAY, "Swap to the best tool in your hotbar when you start breaking a block. Restores the previous slot when you stop.", c -> c.autoTool, (c, v) -> c.autoTool = v),
    AUTO_RESPAWN("auto_respawn", "Auto Respawn", Category.GAMEPLAY, "Automatically respawn after death (survival only).", c -> c.autoRespawn, (c, v) -> c.autoRespawn = v),
    AUTO_REFILL("auto_refill", "Auto Refill", Category.GAMEPLAY, "When a hotbar or off-hand slot runs out, refill it with a matching stack from your inventory.", c -> c.autoRefill, (c, v) -> c.autoRefill = v),
    AUTO_TOTEM("auto_totem", "Auto-Totem", Category.GAMEPLAY, "Swap a totem from your inventory to the off-hand when your health drops below the threshold.", c -> c.autoTotem, (c, v) -> c.autoTotem = v),
    ARMOR_SWAP("armor_swap", "Armor Swap", Category.GAMEPLAY, "Auto-equip the highest-rated armor piece from your inventory whenever a better one is available.", c -> c.armorSwap, (c, v) -> c.armorSwap = v),
    QUICK_STASH("quick_stash", "Quick Stash", Category.GAMEPLAY, "Press the quick-stash key while a container is open to shift every matching stack between your inventory and the container \u2014 direction follows the slot you are hovering.", Kind.TRIGGER, c -> c.quickStash, (c, v) -> c.quickStash = v),
    JUNK_DROP("junk_drop", "Junk Drop", Category.GAMEPLAY, "Drops every blacklisted stack on keybind. Toggle off to ignore the key entirely. Manage the list from the card or /ijunk.", Kind.TRIGGER, c -> c.junkDropEnabled, (c, v) -> c.junkDropEnabled = v),
    CLEAN_SCREENSHOT("clean_screenshot", "Clean Screenshot", Category.GAMEPLAY, "Take a screenshot with the HUD temporarily hidden. Bind a key in Keybinds.", Kind.TRIGGER, c -> c.cleanScreenshot, (c, v) -> c.cleanScreenshot = v),
    ANTI_AFK("anti_afk", "Anti-AFK", Category.GAMEPLAY, "Swing your main hand periodically to avoid AFK kicks. Opt-in; disable on strict anti-cheat servers.", c -> c.antiAfk, (c, v) -> c.antiAfk = v),

    // Visual
    NO_PUMPKIN("no_pumpkin", "No Pumpkin Overlay", Category.VISUAL, "Remove the pumpkin head overlay.", c -> c.noPumpkinOverlay, (c, v) -> c.noPumpkinOverlay = v),
    NO_VIGNETTE("no_vignette", "No Vignette", Category.VISUAL, "Remove the dark screen vignette.", c -> c.noVignette, (c, v) -> c.noVignette = v),
    NO_FIRE_OVERLAY("no_fire_overlay", "No Fire Overlay", Category.VISUAL, "Hide the fire overlay while burning.", c -> c.noFireOverlay, (c, v) -> c.noFireOverlay = v),
    SCOREBOARD_TOGGLE("scoreboard_toggle", "Hide Scoreboard", Category.VISUAL, "Hide the sidebar scoreboard.", c -> c.hideScoreboard, (c, v) -> c.hideScoreboard = v),
    NO_WEATHER("no_weather", "No Weather", Category.VISUAL, "Hide client rain, snow, thunder visuals, sounds and splash particles.", c -> c.noWeather, (c, v) -> c.noWeather = v),
    PARTICLES("particles", "Particle Suppressor", Category.VISUAL, "Hide every particle effect.", c -> c.particlesSuppressed, (c, v) -> c.particlesSuppressed = v),
    NO_FOG("no_fog", "No Fog", Category.VISUAL, "Remove distance fog so terrain stays clear to render limits.", c -> c.noFog, (c, v) -> c.noFog = v),
    NO_LAVA_FOG("no_lava_fog", "No Lava Fog", Category.VISUAL, "Remove the thick fog when your head is submerged in lava.", c -> c.noLavaFog, (c, v) -> c.noLavaFog = v),
    NO_WATER_FOG("no_water_fog", "No Water Fog", Category.VISUAL, "Clear the underwater fog for full visibility.", c -> c.noWaterFog, (c, v) -> c.noWaterFog = v),

    // Technical
    CHUNK_BORDERS("chunk_borders", "Chunk Borders", Category.TECHNICAL, "Render chunk boundaries like F3+G. Mini-map repositionable via HUD Layout.", c -> c.chunkBorders, (c, v) -> c.chunkBorders = v),
    SLIME_CHUNKS("slime_chunks", "Slime Chunks", Category.TECHNICAL, "Highlight slime-spawn chunks. Label + map repositionable via HUD Layout.", c -> c.slimeChunkIndicator, (c, v) -> c.slimeChunkIndicator = v),
    SIGNAL_STRENGTH("signal_strength", "Signal Strength", Category.TECHNICAL, "Show redstone power on blocks. Repositionable via HUD Layout.", c -> c.signalStrengthDisplay, (c, v) -> c.signalStrengthDisplay = v),
    PING("ping", "Ping", Category.TECHNICAL, "Network latency to the server. Repositionable via HUD Layout.", c -> c.showPing, (c, v) -> c.showPing = v),
    TPS("tps", "TPS / MSPT", Category.TECHNICAL, "Server tick performance. Repositionable via HUD Layout.", c -> c.showTps, (c, v) -> c.showTps = v),
    LIGHT_HEATMAP("light_heatmap", "Spawn Heatmap", Category.TECHNICAL, "Color overlay on mob-spawnable blocks (red = always, yellow = marginal).", c -> c.lightHeatmap, (c, v) -> c.lightHeatmap = v),
    HITBOXES("hitboxes", "Entity Hitboxes", Category.TECHNICAL, "Render entity hitboxes.", c -> c.showHitboxes, (c, v) -> c.showHitboxes = v),
    BLOCK_UPDATE_COUNTER("block_update_counter", "Block Update Counter", Category.TECHNICAL, "Count block updates per tick. Repositionable via HUD Layout.", c -> c.blockUpdateCounter, (c, v) -> c.blockUpdateCounter = v),
    VILLAGER_TRADES("villager_trades", "Villager Trades", Category.TECHNICAL, "Show a villager's trades by looking at them or their workstation. Repositionable via HUD Layout.", c -> c.showVillagerTrades, (c, v) -> c.showVillagerTrades = v),
    VILLAGER_AUTO_PROBE("villager_auto_probe", "Villager Auto-Probe", Category.TECHNICAL, "Silently open and close villager trades to auto-capture offers when looking at workstations. Disable on anti-cheat servers.", c -> c.villagerAutoProbe, (c, v) -> c.villagerAutoProbe = v),
    STRUCTURE_OUTLINES("structure_outlines", "Structure Outlines", Category.TECHNICAL, "Wireframe bounding boxes around nearby strongholds, villages, monuments, mansions, fortresses and bastions within 8 chunks. Singleplayer only (reads StructureStart from the integrated server).", c -> c.structureOutlines, (c, v) -> c.structureOutlines = v),
    PISTON_RANGE_PREVIEW("piston_range_preview", "Piston Range Preview", Category.TECHNICAL, "When looking at a non-extended piston, wireframe the 12-block course it could push. Green = free, yellow = pushable, red = immovable.", c -> c.pistonRangePreview, (c, v) -> c.pistonRangePreview = v),
    MOB_ESP("mob_esp", "Mob Highlight", Category.TECHNICAL, "Outline nearby mobs (hostile/neutral/passive colored). Depth-tested by default; toggle X-ray in the card.", c -> c.mobEsp, (c, v) -> c.mobEsp = v),
    BEACON_RANGE("beacon_range", "Beacon Range", Category.TECHNICAL, "Show effect radius for all active beacons within 128 blocks.", c -> c.beaconRangeOverlay, (c, v) -> c.beaconRangeOverlay = v),
    CROP_MATURITY("crop_maturity", "Crop Maturity Overlay", Category.TECHNICAL, "Color a dot above nearby crops: green when mature, yellow when still growing.", c -> c.cropMaturity, (c, v) -> c.cropMaturity = v),
    TRAJECTORY_PREVIEW("trajectory_preview", "Projectile Trajectory", Category.GAMEPLAY, "Show the predicted arc of the projectile fired by the bow / crossbow / trident / throwable you currently hold.", c -> c.trajectoryPreview, (c, v) -> c.trajectoryPreview = v),
    WORLD_STATS("world_stats", "World Stats HUD", Category.TECHNICAL, "Count of rendered entities, block entities and loaded chunks. Repositionable via HUD Layout.", c -> c.showWorldStats, (c, v) -> c.showWorldStats = v),
    SPAWN_SPHERE("spawn_sphere", "Spawning Sphere", Category.TECHNICAL, "Toggle with /ispawnsphere. Render the 24-block no-spawn bubble and the 128-block spawn/despawn shell around the player.", c -> c.spawnSphere, (c, v) -> c.spawnSphere = v),
    LIGHT_LEVEL_NUMBERS("light_level_numbers", "Light Level Numbers", Category.TECHNICAL, "Render the block-light value (0\u20137) in-world on every spawnable surface near the player.", c -> c.lightLevelNumbers, (c, v) -> c.lightLevelNumbers = v),
    FOV_UNLOCK("fov_unlock", "FOV Unlock", Category.VISUAL, "Override the vanilla FOV cap. Set value with /ifov <30..170>.", c -> c.fovOverrideEnabled, (c, v) -> c.fovOverrideEnabled = v);

    public enum Category { HUD, GAMEPLAY, VISUAL, TECHNICAL }

    public enum Kind {
        /** Press the key to flip a persistent on/off state. Default for most features. */
        TOGGLE,
        /** Press the key to run a one-shot action (Junk Drop, Clean Screenshot, Sort, Quick Stash). */
        TRIGGER,
        /** Hold the key for the duration of the effect (Zoom). */
        HOLD
    }

    public final String id;
    public final String displayName;
    public final String description;
    public final Category category;
    public final Kind kind;
    private final Function<IntrinsicConfig, Boolean> getter;
    private final BiConsumer<IntrinsicConfig, Boolean> setter;

    Feature(String id, String displayName, Category category, String description,
            Function<IntrinsicConfig, Boolean> getter,
            BiConsumer<IntrinsicConfig, Boolean> setter) {
        this(id, displayName, category, description, Kind.TOGGLE, getter, setter);
    }

    Feature(String id, String displayName, Category category, String description, Kind kind,
            Function<IntrinsicConfig, Boolean> getter,
            BiConsumer<IntrinsicConfig, Boolean> setter) {
        this.id = id;
        this.displayName = displayName;
        this.category = category;
        this.description = description;
        this.kind = kind;
        this.getter = getter;
        this.setter = setter;
    }

    public boolean isEnabled() {
        return getter.apply(IntrinsicClient.getConfig());
    }

    public void setEnabled(boolean value) {
        setter.accept(IntrinsicClient.getConfig(), value);
    }

    public void toggle() {
        setEnabled(!isEnabled());
    }

    public int getKeyCode() {
        Integer k = IntrinsicClient.getConfig().featureKeybinds.get(id);
        return k == null ? -1 : k;
    }

    public void setKeyCode(int keyCode) {
        if (keyCode < 0) {
            IntrinsicClient.getConfig().featureKeybinds.remove(id);
        } else {
            IntrinsicClient.getConfig().featureKeybinds.put(id, keyCode);
        }
    }
}
