package com.intrinsic.client.mixin;

import com.intrinsic.client.audio.AudioRegistry;
import com.intrinsic.client.feature.NoWeatherFeature;
import com.intrinsic.client.feature.VillagerAutoProbe;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSoundInstance.class)
public class AbstractSoundInstanceMixin {
    @Inject(method = "getVolume", at = @At("RETURN"), cancellable = true)
    private void intrinsic_attenuateVolume(CallbackInfoReturnable<Float> cir) {
        float original = cir.getReturnValueF();
        if (original <= 0.0f) return;
        SoundInstance self = (SoundInstance) (Object) this;
        if (NoWeatherFeature.mutesSound(self)) {
            cir.setReturnValue(0.0f);
            return;
        }
        if (VillagerAutoProbe.mutesSound(self)) {
            cir.setReturnValue(0.0f);
            return;
        }
        float mult = AudioRegistry.getMultiplier(self);
        if (mult == 1.0f) return;
        cir.setReturnValue(original * mult);
    }
}
