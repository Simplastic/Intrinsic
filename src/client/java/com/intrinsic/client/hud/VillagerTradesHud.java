package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.feature.Feature;
import com.intrinsic.client.feature.VillagerAutoProbe;
import com.intrinsic.client.feature.VillagerTradeCache;
import com.intrinsic.client.feature.VillagerWorkstationBinder;
import com.intrinsic.client.gui.widget.ThemeColors;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.trading.MerchantOffer;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.List;
import java.util.UUID;

public final class VillagerTradesHud {
    private static Villager lastTarget = null;
    private static long lastTargetSeenMs = 0L;

    private VillagerTradesHud() {}

    public static Villager currentTarget() {
        if (lastTarget == null) return null;
        if (!lastTarget.isAlive()) { lastTarget = null; return null; }
        if (System.currentTimeMillis() - lastTargetSeenMs > 1500L) return null;
        return lastTarget;
    }

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        if (client.player == null || client.level == null) return;

        HitResult hit = client.hitResult;
        Villager target = null;
        String professionName = null;

        if (hit instanceof EntityHitResult eh && eh.getEntity() instanceof Villager v) {
            if (!VillagerAutoProbe.isEligible(v)) return;
            target = v;
            professionName = professionFor(v);
        } else if (hit instanceof BlockHitResult bh) {
            BlockPos wpos = bh.getBlockPos();
            BlockState state = client.level.getBlockState(wpos);
            if (state == null) return;
            Block block = state.getBlock();
            String expectedProfession = VillagerWorkstationBinder.WorkstationProfessions.professionOf(block);
            if (expectedProfession == null) return;

            UUID owner = VillagerWorkstationBinder.villagerAt(wpos);
            if (owner != null) {
                target = findLivingVillagerByUUID(client, owner);
            }
            if (target == null || !VillagerAutoProbe.isEligible(target)
                    || !expectedProfession.equals(VillagerWorkstationBinder.WorkstationProfessions.professionIdOf(target))) {
                target = findEligibleVillagerFor(client, wpos, expectedProfession);
                if (target == null) return;
            }
            professionName = professionFor(target);
        }

        if (target == null) return;

        lastTarget = target;
        lastTargetSeenMs = System.currentTimeMillis();

        VillagerTradeCache.CachedTrades cached = VillagerTradeCache.get(target.getUUID());
        if (cached == null || cached.offers.isEmpty()) {
            VillagerAutoProbe.maybeProbe(target);
        }
        List<MerchantOffer> offers = cached != null ? cached.offers : List.of();

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int x = HudPositions.x("villagerTrades", sw);
        int y = HudPositions.y("villagerTrades", sh);

        int w = 280;
        int headerH = 16;
        int rowH = 20;
        int visibleRows = offers.size();
        int hintH = offers.isEmpty() ? 14 : 0;
        int h = headerH + (visibleRows > 0 ? visibleRows * rowH + 6 : 0) + hintH + 4;

        HudStyle.card(ctx, x, y, w, h);

        String header = professionName != null ? professionName : "Villager";
        header += "  (lvl " + target.getVillagerData().level() + ")";
        ctx.text(client.font, Component.literal(header), x + 6, y + 4, ThemeColors.TEXT, true);

        int yy = y + headerH;
        if (offers.isEmpty()) {
            String hint = VillagerAutoProbe.isProbing()
                    ? "Capturing trades..."
                    : "Right-click once to capture trades.";
            ctx.text(client.font, Component.literal(hint).withStyle(ChatFormatting.GRAY), x + 6, yy + 2, ThemeColors.TEXT_DIM, true);
            return;
        }

        for (int i = 0; i < visibleRows; i++) {
            MerchantOffer offer = offers.get(i);
            drawOfferRow(ctx, client, offer, x + 4, yy);
            yy += rowH;
        }
    }

    private static void drawOfferRow(GuiGraphicsExtractor ctx, Minecraft client, MerchantOffer offer, int x, int y) {
        ItemStack first = offer.getCostA();
        ItemStack second = offer.getCostB();
        ItemStack sell = offer.getResult();

        ctx.item(first, x, y);
        ctx.itemDecorations(client.font, first, x, y);
        int col = x + 20;

        int priceDelta = offer.getCostA().getCount() - offer.getBaseCostA().getCount();
        if (priceDelta != 0) {
            String indicator = priceDelta < 0 ? "v" : "^";
            int indColor = priceDelta < 0 ? 0xFF55FF55 : 0xFFFF6666;
            ctx.text(client.font, Component.literal(indicator), col - 5, y, indColor, true);
        }

        if (!second.isEmpty()) {
            ctx.item(second, col, y);
            ctx.itemDecorations(client.font, second, col, y);
            col += 20;
        }
        ctx.text(client.font, Component.literal("->"), col, y + 4, ThemeColors.TEXT_DIM, true);
        col += 14;
        ctx.item(sell, col, y);
        ctx.itemDecorations(client.font, sell, col, y);
        col += 22;

        String bookLabel = enchantedBookLabel(sell);
        if (bookLabel != null) {
            int maxLabelW = 90;
            String trimmed = client.font.plainSubstrByWidth(bookLabel, maxLabelW);
            ctx.text(client.font, Component.literal(trimmed), col, y + 4, 0xFFB39DDB, true);
            col += client.font.width(trimmed) + 6;
        }

        Rating rating = rate(offer);
        boolean soldOut = offer.getUses() >= offer.getMaxUses();
        String tag = soldOut ? "Out of stock" : rating.label;
        int tagColor = soldOut ? 0xFFFF5555 : rating.color;
        ctx.text(client.font, Component.literal(tag), col, y + 4, tagColor, true);
    }

    private static String enchantedBookLabel(ItemStack stack) {
        if (!stack.is(Items.ENCHANTED_BOOK)) return null;
        ItemEnchantments enchs = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchs == null || enchs.isEmpty()) return "Enchanted";
        Object2IntMap.Entry<Holder<Enchantment>> entry = enchs.entrySet().iterator().next();
        return Enchantment.getFullname(entry.getKey(), entry.getIntValue()).getString();
    }

    private static String professionFor(Villager v) {
        var prof = v.getVillagerData().profession();
        var key = prof.unwrapKey().orElse(null);
        if (key == null) return "Villager";
        String path = key.identifier().getPath();
        if (path.isEmpty() || path.equals("none")) return "Villager";
        return Character.toUpperCase(path.charAt(0)) + path.substring(1).replace('_', ' ');
    }

    private static Villager findLivingVillagerByUUID(Minecraft client, UUID id) {
        if (client.level == null) return null;
        for (Entity e : client.level.entitiesForRendering()) {
            if (e instanceof Villager v && v.getUUID().equals(id) && v.isAlive()) return v;
        }
        return null;
    }

    private static Villager findEligibleVillagerFor(Minecraft client, BlockPos wpos, String expectedProfession) {
        net.minecraft.client.multiplayer.ClientLevel world = client.level;
        if (world == null) return null;
        AABB box = new AABB(
                wpos.getX() - 16, wpos.getY() - 8, wpos.getZ() - 16,
                wpos.getX() + 17, wpos.getY() + 9, wpos.getZ() + 17);
        net.minecraft.world.level.entity.EntityTypeTest<Entity, Villager> typeTest =
                net.minecraft.world.level.entity.EntityTypeTest.forClass(Villager.class);
        List<Villager> candidates = world.getEntities(typeTest, box, v -> {
            if (!VillagerAutoProbe.isEligible(v)) return false;
            if (!expectedProfession.equals(
                    VillagerWorkstationBinder.WorkstationProfessions.professionIdOf(v))) return false;
            BlockPos bound = VillagerWorkstationBinder.workstationOf(v.getUUID());
            return bound == null || bound.equals(wpos);
        });
        Villager best = null;
        double bestSq = Double.MAX_VALUE;
        for (Villager v : candidates) {
            double dsq = v.distanceToSqr(wpos.getX() + 0.5, wpos.getY() + 0.5, wpos.getZ() + 0.5);
            if (dsq < bestSq) { bestSq = dsq; best = v; }
        }
        return best;
    }

    private enum Rating {
        GREAT("Great deal", 0xFF55FF55),
        FAIR("Fair", 0xFFDDDDDD),
        EXPENSIVE("Expensive", 0xFFFF8866);
        final String label;
        final int color;
        Rating(String label, int color) { this.label = label; this.color = color; }
    }

    private static Rating rate(MerchantOffer offer) {
        ItemStack first = offer.getCostA();
        ItemStack second = offer.getCostB();
        ItemStack sell = offer.getResult();

        int sellCount = Math.max(1, sell.getCount());
        boolean firstEmerald = isEmerald(first);
        boolean secondEmerald = !second.isEmpty() && isEmerald(second);
        boolean sellEmerald = isEmerald(sell);

        if (firstEmerald || secondEmerald) {
            int emeralds = (firstEmerald ? emeraldValue(first) : 0)
                    + (secondEmerald ? emeraldValue(second) : 0);

            if (sell.is(Items.ENCHANTED_BOOK)) {
                int levelSum = enchantedBookLevelSum(sell);
                double perLevel = (double) emeralds / Math.max(1, levelSum);
                if (perLevel <= 7.0) return Rating.GREAT;
                if (perLevel >= 20.0) return Rating.EXPENSIVE;
                return Rating.FAIR;
            }

            double emPerItem = (double) emeralds / sellCount;
            if (emPerItem <= 2.0) return Rating.GREAT;
            if (emPerItem >= 12.0) return Rating.EXPENSIVE;
            return Rating.FAIR;
        }

        if (sellEmerald) {
            int emeralds = emeraldValue(sell);
            int items = first.getCount() + (second.isEmpty() ? 0 : second.getCount());
            double itemsPerEmerald = (double) items / Math.max(1, emeralds);
            if (itemsPerEmerald <= 4.0) return Rating.GREAT;
            if (itemsPerEmerald >= 20.0) return Rating.EXPENSIVE;
            return Rating.FAIR;
        }

        return Rating.FAIR;
    }

    private static boolean isEmerald(ItemStack stack) {
        return stack.getItem() == Items.EMERALD || stack.getItem() == Items.EMERALD_BLOCK;
    }

    private static int emeraldValue(ItemStack stack) {
        if (stack.getItem() == Items.EMERALD_BLOCK) return stack.getCount() * 9;
        return stack.getCount();
    }

    private static int enchantedBookLevelSum(ItemStack stack) {
        ItemEnchantments enchs = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchs == null || enchs.isEmpty()) return 1;
        int sum = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> e : enchs.entrySet()) sum += e.getIntValue();
        return Math.max(1, sum);
    }

    @SuppressWarnings("unused")
    public static boolean isEnabled() {
        return IntrinsicClient.getConfig().showVillagerTrades;
    }
}
