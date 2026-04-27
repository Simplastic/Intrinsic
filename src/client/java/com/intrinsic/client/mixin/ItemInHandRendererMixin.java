package com.intrinsic.client.mixin;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.ItemInHandRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void intrinsic_lowerHeldItem(AbstractClientPlayer player, float tickDelta,
                                           float pitch, InteractionHand hand, float swingProgress,
                                           ItemStack item, float equipProgress,
                                           PoseStack matrices, SubmitNodeCollector queue,
                                           int light, CallbackInfo ci) {
        // Push before translating: the caller keeps a single pose across both hand renders,
        // so without scoping each translate to its own pose the offsets would accumulate.
        matrices.pushPose();

        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        boolean usingThisHand = player.isUsingItem() && player.getUsedItemHand() == hand;

        float offset = 0.0f;
        if (cfg.lowShield && item.getItem() == Items.SHIELD && !usingThisHand) {
            offset = -0.15f;
        } else if (cfg.lowTotem && item.getItem() == Items.TOTEM_OF_UNDYING) {
            offset = -0.20f;
        } else if (cfg.lowFood && item.has(DataComponents.FOOD) && !usingThisHand) {
            offset = -0.15f;
        } else if (cfg.lowPotion && (item.getItem() == Items.POTION
                || item.getItem() == Items.SPLASH_POTION
                || item.getItem() == Items.LINGERING_POTION)) {
            offset = -0.15f;
        }

        if (offset != 0.0f) matrices.translate(0.0f, offset, 0.0f);
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void intrinsic_popHeldItem(AbstractClientPlayer player, float tickDelta,
                                         float pitch, InteractionHand hand, float swingProgress,
                                         ItemStack item, float equipProgress,
                                         PoseStack matrices, SubmitNodeCollector queue,
                                         int light, CallbackInfo ci) {
        matrices.popPose();
    }
}
