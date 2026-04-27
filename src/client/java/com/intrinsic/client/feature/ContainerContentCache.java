package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ContainerContentCache {
    private ContainerContentCache() {}

    public static final class Snapshot {
        public final List<ItemStack> items;
        public final long capturedAtMs;
        public final String title;

        public Snapshot(List<ItemStack> items, long capturedAtMs, String title) {
            this.items = items;
            this.capturedAtMs = capturedAtMs;
            this.title = title;
        }
    }

    private static final Map<String, Snapshot> CACHE = new HashMap<>();

    private static BlockPos pendingPos;
    private static String pendingDim;
    private static String pendingTitle;

    public static void markPending(Level level, BlockPos pos, String title) {
        if (pos == null || level == null) {
            pendingPos = null;
            pendingDim = null;
            pendingTitle = null;
            return;
        }
        pendingPos = pos.immutable();
        pendingDim = level.dimension().identifier().toString();
        pendingTitle = title;
    }

    public static void clearPending() {
        pendingPos = null;
        pendingDim = null;
        pendingTitle = null;
    }

    public static boolean hasPending() {
        return pendingPos != null;
    }

    public static void recordPending(List<ItemStack> items) {
        if (pendingPos == null || pendingDim == null) return;
        String key = key(pendingDim, pendingPos);
        List<ItemStack> copy = new ArrayList<>(items.size());
        for (ItemStack s : items) copy.add(s == null ? ItemStack.EMPTY : s.copy());
        CACHE.put(key, new Snapshot(copy, System.currentTimeMillis(), pendingTitle));
    }

    public static Snapshot get(Level level, BlockPos pos) {
        if (level == null || pos == null) return null;
        evict();
        String key = key(level.dimension().identifier().toString(), pos);
        return CACHE.get(key);
    }

    public static void clearAll() {
        CACHE.clear();
        clearPending();
    }

    private static void evict() {
        long ttlMs = Math.max(1, IntrinsicClient.getConfig().blockContentCacheMinutes) * 60_000L;
        long cutoff = System.currentTimeMillis() - ttlMs;
        CACHE.entrySet().removeIf(e -> e.getValue().capturedAtMs < cutoff);
    }

    private static String key(String dim, BlockPos pos) {
        return dim + "|" + pos.getX() + "|" + pos.getY() + "|" + pos.getZ();
    }

    public static List<ItemStack> emptyList() {
        return Collections.emptyList();
    }
}
