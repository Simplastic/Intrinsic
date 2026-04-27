package com.intrinsic.client.hud;

import com.intrinsic.IntrinsicMod;
import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.feature.ChunkBorderFeature;
import com.intrinsic.client.feature.DurabilityWarningFeature;
import com.intrinsic.client.feature.ElytraAlertFeature;
import com.intrinsic.client.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class IntrinsicHudDispatcher {
    private static long lastErrorLog = 0;

    // LinkedHashMap preserves dispatch order. Order matters for layered HUDs (e.g., LowHpFlash full-screen vignette).
    private static final Map<Feature, BiConsumer<GuiGraphicsExtractor, Minecraft>> RENDERERS = new LinkedHashMap<>();

    static {
        RENDERERS.put(Feature.COORDINATES,            CoordinatesHud::render);
        RENDERERS.put(Feature.FPS,                    FpsHud::render);
        RENDERERS.put(Feature.CLOCK,                  ClockHud::render);
        RENDERERS.put(Feature.BIOME,                  BiomeHud::render);
        RENDERERS.put(Feature.LIGHT_LEVEL,            LightLevelHud::render);
        RENDERERS.put(Feature.DAY_COUNTER,            DayCounterHud::render);
        RENDERERS.put(Feature.DURABILITY_HUD,         DurabilityHud::render);
        RENDERERS.put(Feature.DEATH_COORDS,           DeathCoordsHud::render);
        RENDERERS.put(Feature.ITEM_COUNTER,           ItemCounterHud::render);
        RENDERERS.put(Feature.DURABILITY_WARNING,     DurabilityWarningFeature::render);
        RENDERERS.put(Feature.POTION_TIMERS,          PotionEffectsHud::render);
        RENDERERS.put(Feature.ENTITY_INFO,            EntityInfoHud::render);
        RENDERERS.put(Feature.ARMOR_WARNING,          ArmorWarningHud::render);
        RENDERERS.put(Feature.HUNGER_WARNING,         HungerWarningHud::render);
        RENDERERS.put(Feature.LOW_HP_FLASH,           LowHpFlashHud::render);
        RENDERERS.put(Feature.PING,                   PingHud::render);
        RENDERERS.put(Feature.TPS,                    TpsHud::render);
        RENDERERS.put(Feature.SLIME_CHUNKS,           SlimeChunkHud::render);
        RENDERERS.put(Feature.SIGNAL_STRENGTH,        SignalStrengthHud::render);
        RENDERERS.put(Feature.BLOCK_UPDATE_COUNTER,   BlockUpdateHud::render);
        RENDERERS.put(Feature.CHUNK_BORDERS,          ChunkBorderFeature::render);
        RENDERERS.put(Feature.VILLAGER_TRADES,        VillagerTradesHud::render);
        RENDERERS.put(Feature.BLOCK_INFO,             BlockInfoHud::render);
        RENDERERS.put(Feature.PORTAL_COORDS,          PortalCoordsHud::render);
        RENDERERS.put(Feature.BOOKSHELF_POWER,        BookshelfPowerHud::render);
        RENDERERS.put(Feature.BED_WARNING,            BedWarningHud::render);
        RENDERERS.put(Feature.TOTEM_TRACKER,          TotemTrackerHud::render);
        RENDERERS.put(Feature.SLEEP_TIMER,            SleepTimerHud::render);
        RENDERERS.put(Feature.XP_PROGRESS,            XpProgressHud::render);
        RENDERERS.put(Feature.WAYPOINTS,              WaypointListHud::render);
        RENDERERS.put(Feature.ELYTRA_ALERT,           ElytraAlertFeature::render);
        RENDERERS.put(Feature.INSOMNIA_TRACKER,       InsomniaTrackerHud::render);
        RENDERERS.put(Feature.HOTBAR_TOOLTIP,         HotbarTooltipHud::render);
        RENDERERS.put(Feature.BLOCK_CONTENT_VIEWER,   BlockContentViewerHud::render);
        RENDERERS.put(Feature.DEATH_LOG,              DeathLogHud::render);
        RENDERERS.put(Feature.MOB_HEALTH_BAR,         MobHealthBarHud::render);
        RENDERERS.put(Feature.WORLD_STATS,            WorldStatsHud::render);
    }

    public static void render(GuiGraphicsExtractor context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        if (client.options.hideGui) return;

        // Toggle toast renders regardless of master HUD so the user can see the feedback when toggling master HUD itself.
        safe("ToggleToast", () -> ToggleToastHud.render(context, client));

        if (!IntrinsicClient.getConfig().masterHudEnabled) return;

        for (Map.Entry<Feature, BiConsumer<GuiGraphicsExtractor, Minecraft>> entry : RENDERERS.entrySet()) {
            Feature feature = entry.getKey();
            if (!feature.isEnabled()) continue;
            BiConsumer<GuiGraphicsExtractor, Minecraft> renderer = entry.getValue();
            safe(feature.id, () -> renderer.accept(context, client));
        }
    }

    private static void safe(String name, Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            long now = System.currentTimeMillis();
            if (now - lastErrorLog > 5000) {
                lastErrorLog = now;
                IntrinsicMod.LOGGER.error("Intrinsic HUD '{}' threw", name, t);
            }
        }
    }
}
