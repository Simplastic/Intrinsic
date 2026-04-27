package com.intrinsic.client.hud;

public class HudRenderer {
    public static void register() {
        // HUD rendering now goes through GuiMixin -> IntrinsicHudDispatcher.
        // HudRenderCallback proved unreliable in this fabric-rendering-v1 version.
    }
}
