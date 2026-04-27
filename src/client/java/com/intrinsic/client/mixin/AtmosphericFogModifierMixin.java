package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.NoFogFeature;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AtmosphericFogEnvironment.class)
public abstract class AtmosphericFogModifierMixin {
    @Inject(method = "setupFog", at = @At("TAIL"))
    private void intrinsic_noFog(FogData data, Camera camera, ClientLevel world,
                                    float viewDistance, DeltaTracker counter,
                                    CallbackInfo ci) {
        if (!NoFogFeature.isEnabled()) return;
        float far = Float.MAX_VALUE;
        data.environmentalStart = far;
        data.environmentalEnd = far;
        data.renderDistanceStart = far;
        data.renderDistanceEnd = far;
        data.skyEnd = far;
        data.cloudEnd = far;
    }
}
