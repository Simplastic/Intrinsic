package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.NoWeatherFeature;
import com.intrinsic.client.feature.ParticlesToggleFeature;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public abstract class ParticleManagerMixin {
    @Inject(
            method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"),
            cancellable = true)
    private void intrinsic_blockParticle(ParticleOptions effect,
                                            double x, double y, double z,
                                            double vx, double vy, double vz,
                                            CallbackInfoReturnable<Particle> cir) {
        if (!ParticlesToggleFeature.particlesAllowed()) {
            cir.setReturnValue(null);
            return;
        }
        if (NoWeatherFeature.suppressesParticle(effect)) cir.setReturnValue(null);
    }

}
