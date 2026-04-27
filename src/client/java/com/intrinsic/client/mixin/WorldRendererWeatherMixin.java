package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.NoWeatherFeature;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class WorldRendererWeatherMixin {

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true, require = 0)
    private void intrinsic_skipWeatherRender(CallbackInfo ci) {
        if (NoWeatherFeature.isEnabled()) ci.cancel();
    }
}
