package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;

public final class ParticlesToggleFeature {
    private ParticlesToggleFeature() {}

    public static boolean particlesAllowed() {
        return !IntrinsicClient.getConfig().particlesSuppressed;
    }
}
