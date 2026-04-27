package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.FreecamFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityTurnFreecamMixin {
    @Inject(method = "turn(DD)V", at = @At("HEAD"), cancellable = true)
    private void intrinsic_redirectMouseToFreecam(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (!FreecamFeature.isActive()) return;
        Entity self = (Entity) (Object) this;
        if (self != Minecraft.getInstance().player) return;
        FreecamFeature.applyMouseLook(cursorDeltaX, cursorDeltaY);
        ci.cancel();
    }
}
