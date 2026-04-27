package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.FreecamFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerScrollFreecamMixin {
    @Inject(method = "onScroll(JDD)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void intrinsic_scrollAdjustsFreecamSpeed(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!FreecamFeature.isActive()) return;
        if (Minecraft.getInstance().screen != null) return;
        if (vertical == 0.0) return;
        FreecamFeature.bumpSpeed(vertical);
        ci.cancel();
    }
}
