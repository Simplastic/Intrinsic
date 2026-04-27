package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.ChatHighlightsFeature;
import com.intrinsic.client.feature.ChatTimestampsFeature;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public abstract class ChatHudMixin {
    // Run highlights first so the grey [HH:MM] prefix from the timestamp step
    // is never recolored. Mixin applies @ModifyVariable transforms in the order
    // they appear in the source file.
    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At("HEAD"),
            argsOnly = true,
            require = 1)
    private Component intrinsic_applyHighlights(Component message) {
        if (message == null) return message;
        return ChatHighlightsFeature.apply(message);
    }

    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At("HEAD"),
            argsOnly = true,
            require = 1)
    private Component intrinsic_prefixTimestamp(Component message) {
        if (!ChatTimestampsFeature.isEnabled()) return message;
        if (message == null) return message;
        return ChatTimestampsFeature.prefix(message);
    }
}
