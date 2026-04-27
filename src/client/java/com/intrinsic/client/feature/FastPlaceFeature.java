package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;

public final class FastPlaceFeature {
    private FastPlaceFeature() {}

    public static boolean isEnabled() {
        return IntrinsicClient.getConfig().fastPlace;
    }
}
