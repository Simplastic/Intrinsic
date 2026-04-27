package com.intrinsic.client.mixin;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceAccessor {
    @Accessor("cookingTimer")
    int intrinsic_cookTime();

    @Accessor("cookingTotalTime")
    int intrinsic_cookTimeTotal();
}
