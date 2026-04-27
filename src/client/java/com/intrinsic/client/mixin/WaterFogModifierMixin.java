package com.intrinsic.client.mixin;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WaterFogEnvironment.class)
public abstract class WaterFogModifierMixin {
    @Inject(method = "setupFog", at = @At("TAIL"))
    private void intrinsic_noWaterFog(FogData data, Camera camera, ClientLevel world,
                                         float viewDistance, DeltaTracker counter,
                                         CallbackInfo ci) {
        if (!IntrinsicClient.getConfig().noWaterFog) return;
        float far = Float.MAX_VALUE;
        data.environmentalStart = far;
        data.environmentalEnd = far;
        data.renderDistanceStart = far;
        data.renderDistanceEnd = far;
        data.skyEnd = far;
        data.cloudEnd = far;
    }
}
