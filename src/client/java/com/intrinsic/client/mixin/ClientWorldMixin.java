package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.VillagerWorkstationBinder;
import com.intrinsic.client.hud.BlockUpdateHud;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientWorldMixin {
    @Inject(method = "setServerVerifiedBlockState", at = @At("HEAD"))
    private void intrinsic_captureBlockUpdate(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        if (pos == null) return;
        BlockUpdateHud.recordUpdate(pos.getX() >> 4, pos.getZ() >> 4);
        BlockState oldState = ((Level) (Object) this).getBlockState(pos);
        VillagerWorkstationBinder.onBlockChanged(pos, oldState, state);
    }
}
