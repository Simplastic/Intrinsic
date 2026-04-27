package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.FullbrightFeature;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Lightmap.class)
public class LightTextureMixin {
    @Inject(method = "getBrightness(Lnet/minecraft/world/level/dimension/DimensionType;I)F",
            at = @At("HEAD"), cancellable = true)
    private static void intrinsic_fullbright(DimensionType type, int lightLevel,
                                                CallbackInfoReturnable<Float> cir) {
        if (FullbrightFeature.isEnabled()) {
            cir.setReturnValue(1.0f);
        }
    }
}
