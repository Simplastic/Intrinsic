package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.VillagerInteractionTracker;
import com.intrinsic.client.feature.VillagerTradeCache;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Why: MerchantMenu.getRecipes() only holds populated TradeOffers while a
 * trade GUI is open. We snapshot them on every handledScreenTick while the screen
 * hosts a MerchantMenu so the cache persists beyond the trade session.
 * Targets AbstractContainerScreen (where the method is actually declared) with a type check.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MerchantScreenCaptureMixin {

    @Inject(method = "handledScreenTick", at = @At("TAIL"), require = 0)
    private void intrinsic_captureOffers(CallbackInfo ci) {
        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
        AbstractContainerMenu sh = self.getMenu();
        if (!(sh instanceof MerchantMenu merchant)) return;

        Villager v = VillagerInteractionTracker.lastInteracted();
        if (v == null) return;
        MerchantOffers offers = merchant.getOffers();
        if (offers == null || offers.isEmpty()) return;
        VillagerTradeCache.record(v, offers);
    }
}
