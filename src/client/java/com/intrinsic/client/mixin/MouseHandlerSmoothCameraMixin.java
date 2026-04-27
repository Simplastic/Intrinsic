package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.Feature;
import com.intrinsic.client.feature.FreecamFeature;
import com.intrinsic.client.feature.SmoothCameraFeature;
import com.intrinsic.client.feature.ViewLockFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Targets the single LocalPlayer.turn(DD) call inside MouseHandler.turnPlayer
// so we can rewrite the deltas before vanilla applies them. The bytecode
// call site is declared on LocalPlayer (not Entity), so the redirect target
// must match that descriptor.
@Mixin(MouseHandler.class)
public abstract class MouseHandlerSmoothCameraMixin {
    @Redirect(
            method = "turnPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V")
    )
    private void intrinsic_smoothTurn(LocalPlayer player, double dx, double dy) {
        if (Feature.SMOOTH_CAMERA.isEnabled()
                && !FreecamFeature.isActive()
                && !ViewLockFeature.isActive()
                && player == Minecraft.getInstance().player) {
            player.turn(SmoothCameraFeature.smoothX(dx), SmoothCameraFeature.smoothY(dy));
        } else {
            player.turn(dx, dy);
        }
    }
}
