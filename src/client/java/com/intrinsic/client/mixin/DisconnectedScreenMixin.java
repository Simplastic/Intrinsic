package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.AutoReconnectFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {
    protected DisconnectedScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void intrinsic_addReconnectButton(CallbackInfo ci) {
        if (!AutoReconnectFeature.isEnabled()) return;
        if (AutoReconnectFeature.getLastServer() == null) return;
        Button button = Button.builder(Component.literal("Reconnect"), b ->
                AutoReconnectFeature.reconnect(Minecraft.getInstance(), this))
                .bounds(this.width / 2 - 100, this.height - 30, 200, 20)
                .build();
        this.addRenderableWidget(button);
    }
}
