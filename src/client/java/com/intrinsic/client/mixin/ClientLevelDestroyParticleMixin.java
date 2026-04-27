package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.ParticlesToggleFeature;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Block-breaking puff particles bypass ParticleEngine.createParticle: the
// vanilla path is ClientLevel.addDestroyBlockEffect, which spawns up to 64
// TerrainParticles directly via add(). The createParticle hook in
// ParticleManagerMixin therefore never fires for these — block-break is the
// noisiest particle source in vanilla, so suppression looks broken without
// this hook.
@Mixin(ClientLevel.class)
public abstract class ClientLevelDestroyParticleMixin {
    @Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
    private void intrinsic_suppressBlockDestroy(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!ParticlesToggleFeature.particlesAllowed()) ci.cancel();
    }
}
