package com.intrinsic.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class TpsTracker {
    private static final int SAMPLE_COUNT = 40;
    private static final long[] samples = new long[SAMPLE_COUNT];
    private static int sampleIndex = 0;
    private static int samplesFilled = 0;
    private static long lastWorldTime = -1L;
    private static long lastWallTime = 0L;

    public static void tick(Minecraft client) {
        ClientLevel world = client.level;
        if (world == null || client.hasSingleplayerServer()) {
            reset();
            return;
        }

        long worldTime = world.getGameTime();
        long now = System.currentTimeMillis();

        if (lastWorldTime < 0) {
            lastWorldTime = worldTime;
            lastWallTime = now;
            return;
        }

        long tickDelta = worldTime - lastWorldTime;
        if (tickDelta <= 0) return;

        long wallDelta = now - lastWallTime;
        if (wallDelta <= 0 || tickDelta > 40) {
            lastWorldTime = worldTime;
            lastWallTime = now;
            return;
        }

        long msPerTick = wallDelta / tickDelta;
        samples[sampleIndex] = msPerTick;
        sampleIndex = (sampleIndex + 1) % SAMPLE_COUNT;
        if (samplesFilled < SAMPLE_COUNT) samplesFilled++;

        lastWorldTime = worldTime;
        lastWallTime = now;
    }

    private static void reset() {
        sampleIndex = 0;
        samplesFilled = 0;
        lastWorldTime = -1L;
        lastWallTime = 0L;
    }

    public static double getAverageMspt() {
        if (samplesFilled == 0) return 50.0;
        long total = 0;
        for (int i = 0; i < samplesFilled; i++) total += samples[i];
        return (double) total / samplesFilled;
    }

    public static double getTps() {
        double mspt = getAverageMspt();
        if (mspt <= 0) return 20.0;
        return Math.min(20.0, 1000.0 / mspt);
    }

    public static boolean hasData() {
        return samplesFilled > 0;
    }
}
