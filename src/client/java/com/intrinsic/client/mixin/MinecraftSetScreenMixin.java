package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.VillagerAutoProbe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftSetScreenMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void intrinsic_hideProbeScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof MerchantScreen && VillagerAutoProbe.isProbing()) {
            ci.cancel();
        }
    }
}
