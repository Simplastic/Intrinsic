package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.FreecamFeature;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    // Inject into alignWithEntity rather than update(): update() calls
    // prepareCullFrustum(... position) right after alignWithEntity returns,
    // so overriding at update()-TAIL leaves the frustum built from the
    // player's pose and nothing outside that frustum gets rendered.
    @Inject(method = "alignWithEntity(F)V", at = @At("TAIL"), require = 1)
    private void intrinsic_applyFreecam(float partialTick, CallbackInfo ci) {
        if (!FreecamFeature.isActive()) return;
        FreecamFeature.onRenderFrame();
        CameraAccessor self = (CameraAccessor) this;
        self.intrinsic_setRotation(FreecamFeature.cameraYaw(), FreecamFeature.cameraPitch());
        self.intrinsic_setPos(FreecamFeature.cameraPos());
    }
}
