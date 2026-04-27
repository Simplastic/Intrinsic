package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.ViewLockFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityTurnViewLockMixin {
    @Inject(method = "turn(DD)V", at = @At("HEAD"), cancellable = true)
    private void intrinsic_cancelMouseWhileViewLocked(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (!ViewLockFeature.isActive()) return;
        Entity self = (Entity) (Object) this;
        if (self != Minecraft.getInstance().player) return;
        ci.cancel();
    }
}
