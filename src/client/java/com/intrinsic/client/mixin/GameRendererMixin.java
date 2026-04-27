package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.ZoomFeature;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class GameRendererMixin {
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void intrinsic_modifyFov(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (ZoomFeature.isZooming()) {
            cir.setReturnValue((float) (cir.getReturnValue() * ZoomFeature.getZoomMultiplier()));
        }
    }
}
