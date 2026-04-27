package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.FastPlaceFeature;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientCooldownMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void intrinsic_fastPlaceResetCooldown(CallbackInfo ci) {
        if (!FastPlaceFeature.isEnabled()) return;
        ((MinecraftClientAccessor) this).setItemUseCooldown(0);
    }
}
