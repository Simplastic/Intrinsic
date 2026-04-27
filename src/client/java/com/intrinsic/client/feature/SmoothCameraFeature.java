package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.Minecraft;

// First-order exponential (PT1) smoothing of the raw mouse delta. Called from
// MouseHandlerSmoothCameraMixin when the feature is enabled and neither
// freecam nor view-lock is active.
public final class SmoothCameraFeature {
    private static double pendingX = 0.0;
    private static double pendingY = 0.0;

    private SmoothCameraFeature() {}

    public static double smoothX(double dx) {
        pendingX += dx;
        double out = pendingX * factor();
        pendingX -= out;
        return out;
    }

    public static double smoothY(double dy) {
        pendingY += dy;
        double out = pendingY * factor();
        pendingY -= out;
        return out;
    }

    public static void clear() {
        pendingX = 0.0;
        pendingY = 0.0;
    }

    public static void tick(Minecraft client) {
        if (!Feature.SMOOTH_CAMERA.isEnabled() && (pendingX != 0.0 || pendingY != 0.0)) {
            clear();
        }
    }

    private static double factor() {
        double s = IntrinsicClient.getConfig().smoothCameraStrength;
        if (s < 0.0) s = 0.0;
        if (s > 0.95) s = 0.95;
        return 1.0 - s;
    }
}
