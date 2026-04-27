package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;

public final class NoFogFeature {
    private NoFogFeature() {}

    public static boolean isEnabled() {
        return IntrinsicClient.getConfig().noFog;
    }
}
