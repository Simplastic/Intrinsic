package com.intrinsic.client;

import com.intrinsic.IntrinsicMod;
import com.intrinsic.client.config.IntrinsicConfig;
import com.intrinsic.client.feature.AntiAfkFeature;
import com.intrinsic.client.feature.ArmorSwapFeature;
import com.intrinsic.client.feature.AutoEatFeature;
import com.intrinsic.client.feature.AutoReconnectFeature;
import com.intrinsic.client.feature.AutoRefillFeature;
import com.intrinsic.client.feature.AutoRespawnFeature;
import com.intrinsic.client.feature.AutoToolFeature;
import com.intrinsic.client.feature.AutoTotemFeature;
import com.intrinsic.client.feature.FovUnlockFeature;
import com.intrinsic.client.feature.LightLevelNumbersFeature;
import com.intrinsic.client.feature.CleanScreenshotFeature;
import com.intrinsic.client.feature.CropMaturityFeature;
import com.intrinsic.client.feature.DamagePopupFeature;
import com.intrinsic.client.feature.HeldItemTooltipTracker;
import com.intrinsic.client.feature.BeaconRangeFeature;
import com.intrinsic.client.feature.ChunkBorderFeature;
import com.intrinsic.client.feature.DurabilityWarningFeature;
import com.intrinsic.client.feature.ElytraAlertFeature;
import com.intrinsic.client.feature.Feature;
import com.intrinsic.client.feature.FeatureHotkeyHandler;
import com.intrinsic.client.feature.FreecamFeature;
import com.intrinsic.client.feature.FullbrightFeature;
import com.intrinsic.client.feature.HitboxFeature;
import com.intrinsic.client.feature.JunkDropFeature;
import com.intrinsic.client.feature.LightHeatmapFeature;
import com.intrinsic.client.feature.MobEspFeature;
import com.intrinsic.client.feature.NetherCoordsCommand;
import com.intrinsic.client.feature.PistonRangePreviewFeature;
import com.intrinsic.client.feature.SlimeSeedCommand;
import com.intrinsic.client.feature.SmoothCameraFeature;
import com.intrinsic.client.feature.StructureOutlinesFeature;
import com.intrinsic.client.feature.ShulkerTooltipSuppressor;
import com.intrinsic.client.feature.SpawningSphereFeature;
import com.intrinsic.client.feature.SprintToggleFeature;
import com.intrinsic.client.feature.TrajectoryPreviewFeature;
import com.intrinsic.client.feature.ViewLockFeature;
import com.intrinsic.client.feature.VillagerAutoProbe;
import com.intrinsic.client.feature.VillagerHighlightFeature;
import com.intrinsic.client.feature.VillagerWorkstationBinder;
import com.intrinsic.client.feature.WaypointsFeature;
import com.intrinsic.client.feature.ZoomFeature;
import com.intrinsic.client.hud.DeathCoordsHud;
import com.intrinsic.client.hud.HudRenderer;
import com.intrinsic.client.keybind.ModKeybinds;
import com.intrinsic.client.resource.TexturePacks;
import com.intrinsic.client.util.TpsTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class IntrinsicClient implements ClientModInitializer {
    private static IntrinsicConfig config;

    @Override
    public void onInitializeClient() {
        config = IntrinsicConfig.load();
        ModKeybinds.register();
        HudRenderer.register();
        ShulkerTooltipSuppressor.register();
        ChunkBorderFeature.register();
        SlimeSeedCommand.register();
        NetherCoordsCommand.register();
        LightHeatmapFeature.register();
        MobEspFeature.register();
        BeaconRangeFeature.register();
        WaypointsFeature.register();
        CropMaturityFeature.register();
        FreecamFeature.register();
        AutoRefillFeature.register();
        VillagerAutoProbe.register();
        VillagerWorkstationBinder.register();
        VillagerHighlightFeature.register();
        TrajectoryPreviewFeature.register();
        SpawningSphereFeature.register();
        StructureOutlinesFeature.register();
        PistonRangePreviewFeature.register();
        JunkDropFeature.register();
        FovUnlockFeature.register();
        LightLevelNumbersFeature.register();
        DamagePopupFeature.register();
        TexturePacks.registerAll();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModKeybinds.handleTick(client);
            FeatureHotkeyHandler.tick(client);
            SprintToggleFeature.tick(client);
            ZoomFeature.tick(client);
            DurabilityWarningFeature.tick(client);
            DeathCoordsHud.tick(client);
            HitboxFeature.tick(client);
            FullbrightFeature.tick(client);
            ElytraAlertFeature.tick(client);
            LightHeatmapFeature.tick(client);
            StructureOutlinesFeature.tick(client);
            AutoEatFeature.tick(client);
            AutoToolFeature.tick(client);
            AutoTotemFeature.tick(client);
            ArmorSwapFeature.tick(client);
            DamagePopupFeature.tick(client);
            ViewLockFeature.tick(client);
            FovUnlockFeature.tick(client);
            LightLevelNumbersFeature.tick(client);
            AutoRespawnFeature.tick(client);
            CleanScreenshotFeature.tick(client);
            AntiAfkFeature.tick(client);
            CropMaturityFeature.tick(client);
            HeldItemTooltipTracker.tick(client);
            SmoothCameraFeature.tick(client);
            TpsTracker.tick(client);
            if (client.getConnection() != null) {
                AutoReconnectFeature.recordServer(client.getCurrentServer());
            }
        });

        IntrinsicMod.LOGGER.info("Intrinsic client initialized with {} features", Feature.values().length);
    }

    public static IntrinsicConfig getConfig() {
        return config;
    }
}
