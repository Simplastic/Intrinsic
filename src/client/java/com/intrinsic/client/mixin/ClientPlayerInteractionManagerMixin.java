package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.FastBreakFeature;
import com.intrinsic.client.feature.VillagerInteractionTracker;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void intrinsic_fastBreakResetCooldown(CallbackInfo ci) {
        if (!FastBreakFeature.isEnabled()) return;
        ((ClientPlayerInteractionManagerAccessor) this).setBlockBreakingCooldown(0);
    }

    @Inject(method = "interactEntity", at = @At("HEAD"), require = 0)
    private void intrinsic_trackVillagerInteract(Player player, Entity entity, InteractionHand hand,
                                                    CallbackInfoReturnable<InteractionResult> cir) {
        if (entity instanceof Villager v) {
            VillagerInteractionTracker.record(v);
        }
    }
}
