package com.intrinsic.client.feature;

import net.minecraft.world.entity.npc.villager.Villager;

import java.lang.ref.WeakReference;

public final class VillagerInteractionTracker {
    private static WeakReference<Villager> last = new WeakReference<>(null);
    private static long lastRecordedAtMs = 0L;

    private VillagerInteractionTracker() {}

    public static void record(Villager villager) {
        last = new WeakReference<>(villager);
        lastRecordedAtMs = System.currentTimeMillis();
    }

    public static Villager lastInteracted() {
        return last.get();
    }

    public static boolean isFresh(long maxAgeMs) {
        return lastRecordedAtMs != 0L
                && System.currentTimeMillis() - lastRecordedAtMs <= maxAgeMs;
    }

    public static void clear() {
        last = new WeakReference<>(null);
        lastRecordedAtMs = 0L;
    }
}
