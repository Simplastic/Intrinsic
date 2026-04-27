package com.intrinsic.client.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int intrinsic_leftPos();

    @Accessor("topPos")
    int intrinsic_topPos();

    @Accessor("hoveredSlot")
    Slot intrinsic_hoveredSlot();
}
