package com.intrinsic.client.feature;

import com.intrinsic.IntrinsicMod;
import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.ItemCost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class VillagerTradeCache {
    private static final int MAX_ENTRIES = 128;
    private static final long STALE_MS = 10L * 60L * 1000L;

    private static final Map<UUID, CachedTrades> LIVE = new HashMap<>();
    private static boolean hydrated = false;

    private VillagerTradeCache() {}

    public static final class CachedTrades {
        public final String professionId;
        public final int level;
        public final List<MerchantOffer> offers;
        public final long capturedAtMs;

        CachedTrades(String professionId, int level, List<MerchantOffer> offers, long capturedAtMs) {
            this.professionId = professionId;
            this.level = level;
            this.offers = offers;
            this.capturedAtMs = capturedAtMs;
        }
    }

    public static void record(Villager v, MerchantOffers offers) {
        if (v == null || offers == null || offers.isEmpty()) return;
        String prof = professionId(v);
        int level = v.getVillagerData().level();
        long now = System.currentTimeMillis();
        List<MerchantOffer> copy = new ArrayList<>(offers);
        LIVE.put(v.getUUID(), new CachedTrades(prof, level, copy, now));
        trimAndPersist();
    }

    public static CachedTrades get(UUID id) {
        ensureHydrated();
        return LIVE.get(id);
    }

    public static void clear() {
        LIVE.clear();
        IntrinsicClient.getConfig().villagerTradeCache.clear();
        IntrinsicClient.getConfig().save();
    }

    public static void invalidate(UUID id) {
        if (id == null) return;
        CachedTrades removed = LIVE.remove(id);
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        if (cfg.villagerTradeCache.remove(id.toString()) != null || removed != null) {
            cfg.save();
        }
    }

    private static void ensureHydrated() {
        if (hydrated) return;
        hydrated = true;
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        long now = System.currentTimeMillis();
        for (Map.Entry<String, IntrinsicConfig.SerializedTrades> e : cfg.villagerTradeCache.entrySet()) {
            try {
                UUID id = UUID.fromString(e.getKey());
                IntrinsicConfig.SerializedTrades s = e.getValue();
                if (s == null || s.offers == null) continue;
                if (now - s.capturedAtMs > STALE_MS * 6) continue;
                List<MerchantOffer> offers = new ArrayList<>();
                for (IntrinsicConfig.SerializedOffer o : s.offers) {
                    MerchantOffer offer = deserializeOffer(o);
                    if (offer != null) offers.add(offer);
                }
                if (!offers.isEmpty()) {
                    LIVE.put(id, new CachedTrades(s.professionId, s.level, offers, s.capturedAtMs));
                }
            } catch (Throwable t) {
                IntrinsicMod.LOGGER.warn("Skipping corrupt villager cache entry {}", e.getKey());
            }
        }
    }

    private static void trimAndPersist() {
        if (LIVE.size() > MAX_ENTRIES) {
            List<Map.Entry<UUID, CachedTrades>> sorted = new ArrayList<>(LIVE.entrySet());
            sorted.sort((a, b) -> Long.compare(a.getValue().capturedAtMs, b.getValue().capturedAtMs));
            int remove = LIVE.size() - MAX_ENTRIES;
            for (int i = 0; i < remove; i++) LIVE.remove(sorted.get(i).getKey());
        }
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        cfg.villagerTradeCache.clear();
        for (Map.Entry<UUID, CachedTrades> e : LIVE.entrySet()) {
            IntrinsicConfig.SerializedTrades s = new IntrinsicConfig.SerializedTrades();
            s.professionId = e.getValue().professionId;
            s.level = e.getValue().level;
            s.capturedAtMs = e.getValue().capturedAtMs;
            for (MerchantOffer off : e.getValue().offers) {
                s.offers.add(serializeOffer(off));
            }
            cfg.villagerTradeCache.put(e.getKey().toString(), s);
        }
        cfg.save();
    }

    private static IntrinsicConfig.SerializedOffer serializeOffer(MerchantOffer offer) {
        IntrinsicConfig.SerializedOffer o = new IntrinsicConfig.SerializedOffer();
        ItemCost first = offer.getItemCostA();
        o.firstBuyItem = first != null ? BuiltInRegistries.ITEM.getKey(first.itemStack().getItem()).toString() : "minecraft:air";
        o.firstBuyCount = first != null ? first.count() : 0;
        var second = offer.getItemCostB();
        if (second.isPresent()) {
            o.secondBuyItem = BuiltInRegistries.ITEM.getKey(second.get().itemStack().getItem()).toString();
            o.secondBuyCount = second.get().count();
        } else {
            o.secondBuyItem = "minecraft:air";
            o.secondBuyCount = 0;
        }
        ItemStack sell = offer.getResult();
        o.sellItem = BuiltInRegistries.ITEM.getKey(sell.getItem()).toString();
        o.sellCount = sell.getCount();
        o.uses = offer.getUses();
        o.maxUses = offer.getMaxUses();
        return o;
    }

    private static MerchantOffer deserializeOffer(IntrinsicConfig.SerializedOffer o) {
        try {
            ItemCost first = new ItemCost(BuiltInRegistries.ITEM.getValue(Identifier.parse(o.firstBuyItem)), Math.max(1, o.firstBuyCount));
            java.util.Optional<ItemCost> second = java.util.Optional.empty();
            if (o.secondBuyItem != null && !o.secondBuyItem.equals("minecraft:air") && o.secondBuyCount > 0) {
                second = java.util.Optional.of(new ItemCost(BuiltInRegistries.ITEM.getValue(Identifier.parse(o.secondBuyItem)), o.secondBuyCount));
            }
            ItemStack sell = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(o.sellItem)), Math.max(1, o.sellCount));
            return new MerchantOffer(first, second, sell, o.uses, Math.max(1, o.maxUses), 0, 0f, 0);
        } catch (Throwable t) {
            return null;
        }
    }

    private static String professionId(Villager v) {
        var prof = v.getVillagerData().profession();
        if (prof.unwrapKey().isPresent()) return prof.unwrapKey().get().identifier().toString();
        return "minecraft:none";
    }
}
