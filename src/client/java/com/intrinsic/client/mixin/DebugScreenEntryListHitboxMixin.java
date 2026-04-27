package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.Feature;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DebugScreenEntryList.class)
public abstract class DebugScreenEntryListHitboxMixin {
    @Inject(method = "isCurrentlyEnabled", at = @At("RETURN"), cancellable = true)
    private void intrinsic_orHitboxToggle(Identifier id, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;
        if (id.equals(DebugScreenEntries.ENTITY_HITBOXES) && Feature.HITBOXES.isEnabled()) {
            cir.setReturnValue(true);
        }
    }
}
