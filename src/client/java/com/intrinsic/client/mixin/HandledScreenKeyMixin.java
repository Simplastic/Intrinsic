package com.intrinsic.client.mixin;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.feature.Feature;
import com.intrinsic.client.feature.InventorySortFeature;
import com.intrinsic.client.feature.QuickStashFeature;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenKeyMixin {
    @Shadow
    protected Slot hoveredSlot;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void intrinsic_sortKeyInScreen(KeyEvent input,
                                              CallbackInfoReturnable<Boolean> cir) {
        if (!IntrinsicClient.getConfig().inventorySort) return;
        int bound = Feature.INVENTORY_SORT.getKeyCode();
        if (bound < 0 || input.key() != bound) return;
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        if (InventorySortFeature.trigger(screen, hoveredSlot)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void intrinsic_quickStashKeyInScreen(KeyEvent input,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (!IntrinsicClient.getConfig().quickStash) return;
        int bound = Feature.QUICK_STASH.getKeyCode();
        if (bound < 0 || input.key() != bound) return;
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        if (QuickStashFeature.trigger(screen, hoveredSlot)) {
            cir.setReturnValue(true);
        }
    }
}
