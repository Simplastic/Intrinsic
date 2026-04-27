package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

public class FullbrightFeature {
    private static Double savedGamma = null;
    private static boolean wasEnabled = false;

    public static void toggle() {
        IntrinsicConfig config = IntrinsicClient.getConfig();
        config.fullbright = !config.fullbright;
    }

    public static boolean isEnabled() {
        return IntrinsicClient.getConfig().fullbright;
    }

    public static void tick(Minecraft client) {
        if (client == null || client.options == null) return;
        OptionInstance<Double> gamma;
        try {
            gamma = client.options.gamma();
        } catch (Throwable t) {
            return;
        }
        boolean enabled = isEnabled();
        if (enabled && !wasEnabled) {
            savedGamma = gamma.get();
            wasEnabled = true;
        }
        if (!enabled && wasEnabled) {
            if (savedGamma != null) {
                setGamma(gamma, savedGamma);
            }
            savedGamma = null;
            wasEnabled = false;
            return;
        }
        if (enabled) {
            if (gamma.get() < 14.9) {
                setGamma(gamma, 15.0);
            }
        }
    }

    private static void setGamma(OptionInstance<Double> gamma, double value) {
        try {
            // OptionInstance validates against its callbacks; bypass via accessor mixin.
            com.intrinsic.client.mixin.SimpleOptionAccessor acc =
                    (com.intrinsic.client.mixin.SimpleOptionAccessor) (Object) gamma;
            acc.intrinsic_setValue(value);
        } catch (Throwable t) {
            // Fall back to the normal setter (will clamp to [0, 1])
            gamma.set(value);
        }
    }
}
