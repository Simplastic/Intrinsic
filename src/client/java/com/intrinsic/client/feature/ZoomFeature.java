package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

public class ZoomFeature {
    private static final long DURATION_NANOS = 150_000_000L; // 150ms

    private static double fromMultiplier = 1.0;
    private static double targetMultiplier = 1.0;
    private static long zoomStartNanos = System.nanoTime() - DURATION_NANOS;

    private static double originalSensitivity = -1;

    public static void tick(Minecraft client) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        int keyCode = Feature.ZOOM.getKeyCode();
        boolean shouldZoom = cfg.zoomEnabled
                && client.screen == null
                && keyCode >= 0
                && InputConstants.isKeyDown(client.getWindow(), keyCode);

        double desired = shouldZoom ? (1.0 / cfg.zoomLevel) : 1.0;

        if (desired != targetMultiplier) {
            fromMultiplier = getZoomMultiplier();
            targetMultiplier = desired;
            zoomStartNanos = System.nanoTime();

            if (shouldZoom && originalSensitivity < 0) {
                originalSensitivity = client.options.sensitivity().get();
                client.options.sensitivity().set(originalSensitivity / cfg.zoomLevel);
            } else if (!shouldZoom && originalSensitivity >= 0) {
                client.options.sensitivity().set(originalSensitivity);
                originalSensitivity = -1;
            }
        }
    }

    public static double getZoomMultiplier() {
        long elapsed = System.nanoTime() - zoomStartNanos;
        if (elapsed >= DURATION_NANOS) return targetMultiplier;
        if (elapsed <= 0) return fromMultiplier;
        double t = (double) elapsed / DURATION_NANOS;
        double eased = easeInOutCubic(t);
        return fromMultiplier + (targetMultiplier - fromMultiplier) * eased;
    }

    public static boolean isZooming() {
        return Math.abs(getZoomMultiplier() - 1.0) > 0.0005;
    }

    private static double easeInOutCubic(double t) {
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }
}
