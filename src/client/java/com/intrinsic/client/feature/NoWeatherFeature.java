package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;

public final class NoWeatherFeature {
    private NoWeatherFeature() {}

    public static boolean isEnabled() {
        return IntrinsicClient.getConfig().noWeather;
    }

    public static boolean mutesSound(SoundInstance instance) {
        if (!isEnabled()) return false;
        Identifier id = instance.getIdentifier();
        if (id == null) return false;
        String s = id.getPath();
        return s.startsWith("weather.rain")
                || s.startsWith("entity.lightning_bolt")
                || s.startsWith("ambient.weather");
    }

    public static boolean suppressesParticle(ParticleOptions effect) {
        if (!isEnabled() || effect == null) return false;
        var type = effect.getType();
        return type == ParticleTypes.RAIN
                || type == ParticleTypes.SPLASH
                || type == ParticleTypes.DRIPPING_WATER;
    }
}
