package com.intrinsic.client.mixin;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class InGameOverlayRendererMixin {
    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void intrinsic_noFireOverlay(PoseStack matrices, MultiBufferSource vertexConsumers,
                                                   TextureAtlasSprite sprite, CallbackInfo ci) {
        if (IntrinsicClient.getConfig().noFireOverlay) {
            ci.cancel();
        }
    }
}
