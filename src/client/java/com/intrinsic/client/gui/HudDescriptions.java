package com.intrinsic.client.gui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HudDescriptions {
    private static final Map<String, String> DESC = new LinkedHashMap<>();

    static {
        DESC.put("coordinates",       "X / Y / Z and facing direction.");
        DESC.put("biome",             "Current biome name.");
        DESC.put("lightLevel",        "Feet + head light level, sky vs block.");
        DESC.put("fps",               "Live frames per second.");
        DESC.put("clock",             "In-game clock (HH:MM + day).");
        DESC.put("dayCounter",        "Days since world creation.");
        DESC.put("ping",              "Network latency to the server.");
        DESC.put("tps",               "Server TPS + MSPT.");
        DESC.put("chunkBorders",      "Mini chunk-border map with player dot.");
        DESC.put("slimeChunkMap",     "Local slime-chunk grid around you.");
        DESC.put("durability",        "Armor + tool durability bars.");
        DESC.put("deathCoords",       "Last death coordinates.");
        DESC.put("blockUpdate",       "Block updates per tick.");
        DESC.put("villagerTrades",    "Villager trade predictions & deal ratings.");
        DESC.put("potionEffects",     "Active status effect timers.");
        DESC.put("itemCounter",       "Count of the item you hold.");
        DESC.put("entityInfo",        "Info card for the entity you look at.");
        DESC.put("signalStrength",    "Redstone signal strength on target block.");
        DESC.put("durabilityWarning", "Popup when a tool is near broken.");
        DESC.put("armorWarning",      "Popup when armor is near broken.");
        DESC.put("hungerWarning",     "Popup when hunger is low.");
        DESC.put("blockInfo",         "Contextual info on the block you look at.");
        DESC.put("toggleToast",       "Transient popup when a feature is toggled via keybind.");
        DESC.put("totemTracker",      "Totem of Undying count across your inventory.");
        DESC.put("sleepTimer",        "Dawn countdown while sleeping, danger scan at night.");
        DESC.put("xpProgress",        "XP level, current progress, points to the next level.");
        DESC.put("waypointList",      "Top 5 nearest waypoints with distance.");
        DESC.put("elytraAlert",       "Warning banner when elytra durability is low.");
        DESC.put("insomniaTracker",   "Phantom spawn readiness: time since rest + night/sky conditions.");
        DESC.put("hotbarTooltip",     "Item name, enchantments and durability above the hotbar; replaces the vanilla popup.");
        DESC.put("blockContent",      "Remembered contents of the container block you look at.");
        DESC.put("deathLog",          "List of your last 5 deaths with timestamps.");
        DESC.put("mobHealthBar",      "Health bar above the crosshair for the entity you look at.");
        DESC.put("worldStats",        "Counts of rendered entities, loaded chunks and nearby players.");
        DESC.put("portalCoords",      "Overworld ↔ Nether coordinate conversion when looking at a portal.");
        DESC.put("bookshelfPower",    "Count of bookshelves powering the enchanting table you look at.");
        DESC.put("bedWarning",        "Warn when looking at a bed in Nether or End (it explodes).");
    }

    public static String get(String id) {
        return DESC.getOrDefault(id, "");
    }

    private HudDescriptions() {}
}
