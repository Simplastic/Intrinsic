package com.intrinsic.client.mixin;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientInput.class)
public interface FreecamInputAccessor {
    @Accessor("moveVector")
    void intrinsic_setMovementVector(Vec2 value);
}
