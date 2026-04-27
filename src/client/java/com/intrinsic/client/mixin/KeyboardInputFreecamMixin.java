package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.FreecamFeature;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputFreecamMixin {
    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void intrinsic_muteInputForFreecam(CallbackInfo ci) {
        if (!FreecamFeature.isActive()) return;
        ClientInput self = (ClientInput) (Object) this;
        self.keyPresses = Input.EMPTY;
        ((FreecamInputAccessor) self).intrinsic_setMovementVector(Vec2.ZERO);
    }
}
