package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.Minecraft;

public final class FastBreakFeature {
    private FastBreakFeature() {}

    public static boolean isEnabled() {
        if (!IntrinsicClient.getConfig().fastBreak) return false;
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.isCreative();
    }
}
