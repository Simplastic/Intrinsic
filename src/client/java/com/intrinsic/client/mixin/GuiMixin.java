package com.intrinsic.client.mixin;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import com.intrinsic.client.feature.Feature;
import com.intrinsic.client.hud.IntrinsicHudDispatcher;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("TAIL"))
    private void intrinsic_renderHud(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        IntrinsicHudDispatcher.render(context);
    }

    @Inject(method = "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/scores/Objective;)V",
            at = @At("HEAD"), cancellable = true)
    private void intrinsic_hideScoreboard(GuiGraphicsExtractor context, Objective objective, CallbackInfo ci) {
        if (IntrinsicClient.getConfig().hideScoreboard) {
            ci.cancel();
        }
    }

    @Inject(method = "extractTextureOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;F)V",
            at = @At("HEAD"), cancellable = true)
    private void intrinsic_noPumpkinOverlay(GuiGraphicsExtractor context, Identifier texture, float opacity, CallbackInfo ci) {
        IntrinsicConfig config = IntrinsicClient.getConfig();
        if (config.noPumpkinOverlay && texture.getPath().contains("pumpkinblur")) {
            ci.cancel();
        }
    }

    @Inject(method = "extractVignette(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"), cancellable = true)
    private void intrinsic_noVignette(GuiGraphicsExtractor context, Entity entity, CallbackInfo ci) {
        if (IntrinsicClient.getConfig().noVignette) {
            ci.cancel();
        }
    }

    @Inject(method = "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("HEAD"), cancellable = true)
    private void intrinsic_suppressSelectedItemName(GuiGraphicsExtractor context, CallbackInfo ci) {
        if (IntrinsicClient.getConfig().masterHudEnabled && Feature.HOTBAR_TOOLTIP.isEnabled()) {
            ci.cancel();
        }
    }
}
