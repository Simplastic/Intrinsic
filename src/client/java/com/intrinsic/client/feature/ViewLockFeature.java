package com.intrinsic.client.feature;

import com.intrinsic.client.hud.ToggleToastHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

// Freezes the player's yaw/pitch at a chosen angle. Mouse input is cancelled
// by EntityTurnViewLockMixin; we also re-apply the rotation each tick so any
// server-side teleport / knockback pushback is absorbed on the next frame.
public final class ViewLockFeature {
    private static boolean active;
    private static float lockedYaw;
    private static float lockedPitch;

    private ViewLockFeature() {}

    public static boolean isActive() {
        return active && Feature.VIEW_LOCK.isEnabled();
    }

    public static void toggle(Minecraft client) {
        if (!Feature.VIEW_LOCK.isEnabled()) return;
        LocalPlayer player = client.player;
        if (player == null) return;
        if (active) {
            active = false;
        } else {
            lockedYaw = player.getYRot();
            lockedPitch = player.getXRot();
            active = true;
        }
        ToggleToastHud.push(Feature.VIEW_LOCK);
    }

    public static void tick(Minecraft client) {
        if (!isActive()) return;
        LocalPlayer player = client.player;
        if (player == null) return;
        player.setYRot(lockedYaw);
        player.setXRot(lockedPitch);
        // Overwrite the "previous-frame" rotations too, else the render-time
        // interpolation will lerp from the mouse-moved value to our locked
        // value and produce a one-frame judder.
        player.yRotO = lockedYaw;
        player.xRotO = lockedPitch;
    }
}
