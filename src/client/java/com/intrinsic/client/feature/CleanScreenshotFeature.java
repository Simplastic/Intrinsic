package com.intrinsic.client.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

public final class CleanScreenshotFeature {
    private CleanScreenshotFeature() {}

    private static int pendingTicks = -1;
    private static boolean prevHideGui = false;

    /** Hotkey trigger entry. Starts a 2-tick delay so the next frame renders without the HUD. */
    public static void takeNow(Minecraft client) {
        if (!Feature.CLEAN_SCREENSHOT.isEnabled()) return;
        if (pendingTicks > 0) return;
        prevHideGui = client.options.hideGui;
        client.options.hideGui = true;
        pendingTicks = 2;
    }

    public static void tick(Minecraft client) {
        if (pendingTicks > 0) {
            if (--pendingTicks == 0) {
                try {
                    Screenshot.grab(
                            client.gameDirectory,
                            client.getMainRenderTarget(),
                            msg -> {
                                if (client.player != null) client.player.sendSystemMessage(msg);
                            }
                    );
                } finally {
                    client.options.hideGui = prevHideGui;
                }
            }
        }
    }
}
