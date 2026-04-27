package com.intrinsic.client.hud;

import com.intrinsic.client.feature.Feature;
import com.intrinsic.client.feature.HeldItemTooltipTracker;
import com.intrinsic.client.gui.widget.ThemeColors;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class HotbarTooltipHud {
    private HotbarTooltipHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        if (client.player == null) return;

        Source src = resolveSource(client);
        if (src == null) return;

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("hotbarTooltip");
        int anchorX = HudPositions.x("hotbarTooltip", sw);
        int anchorY = HudPositions.y("hotbarTooltip", sh);
        int centerX = anchorX + a.width / 2;

        ItemEnchantments enchants = effectiveEnchantments(src.stack);
        boolean damageable = src.stack.isDamageableItem() && src.stack.getMaxDamage() > 0;

        Component nameComp = src.stack.getHoverName();
        int nameWidth = client.font.width(nameComp);

        int rows = enchants.size();
        int contentHeight = 12 + (rows > 0 ? rows * 10 + 2 : 0) + (damageable ? 5 : 0);
        int padX = 6;
        int cardWidth = Math.max(nameWidth + padX * 2, 120);
        for (Object2IntMap.Entry<Holder<Enchantment>> e : enchants.entrySet()) {
            String line = nameOf(e.getKey()) + " " + roman(e.getIntValue());
            cardWidth = Math.max(cardWidth, client.font.width(line) + padX * 2);
        }
        cardWidth = Math.min(cardWidth, a.width);

        int x0 = centerX - cardWidth / 2;
        int y0 = anchorY;
        int x1 = x0 + cardWidth;
        int y1 = y0 + contentHeight;

        int bgAlpha = Math.round(src.alpha * 0xCC);
        int textAlpha = Math.round(src.alpha * 0xFF);
        if (bgAlpha < 16) return;

        int bgColor = (bgAlpha << 24) | 0x0B0E14;
        int accentColor = (textAlpha << 24) | (ThemeColors.ACCENT & 0x00FFFFFF);
        int textColor = (textAlpha << 24) | (ThemeColors.TEXT & 0x00FFFFFF);
        int dimColor = (textAlpha << 24) | (ThemeColors.TEXT_DIM & 0x00FFFFFF);

        ctx.fill(x0, y0, x1, y1, bgColor);
        ctx.fill(x0, y0, x1, y0 + 1, accentColor);

        int yy = y0 + 3;
        ctx.text(client.font, nameComp, centerX - nameWidth / 2, yy, textColor, true);
        yy += 11;

        for (Object2IntMap.Entry<Holder<Enchantment>> e : enchants.entrySet()) {
            String line = nameOf(e.getKey()) + " " + roman(e.getIntValue());
            Component styled = Component.literal(line).withStyle(ChatFormatting.AQUA);
            int lw = client.font.width(styled);
            ctx.text(client.font, styled, centerX - lw / 2, yy, dimColor, true);
            yy += 10;
        }

        if (damageable) {
            int max = src.stack.getMaxDamage();
            int cur = Math.max(0, max - src.stack.getDamageValue());
            int barW = cardWidth - padX * 2;
            int barX = x0 + padX;
            int barY = y1 - 4;
            int filled = Math.round(barW * (cur / (float) max));
            int durColor = durabilityColor(cur / (float) max, textAlpha);
            ctx.fill(barX, barY, barX + barW, barY + 2, (textAlpha / 3 << 24) | 0x202635);
            ctx.fill(barX, barY, barX + filled, barY + 2, durColor);
        }
    }

    private static int durabilityColor(float frac, int alpha) {
        int r, g;
        if (frac > 0.5f) {
            float t = (1.0f - frac) * 2f;
            r = (int) (0x10 + (0xFF - 0x10) * t);
            g = 0xC8;
        } else {
            float t = (0.5f - frac) * 2f;
            r = 0xFF;
            g = (int) (0xC8 * (1.0f - t) + 0x30 * t);
        }
        return (alpha << 24) | (r << 16) | (g << 8) | 0x30;
    }

    private record Source(ItemStack stack, float alpha) {}

    private static Source resolveSource(Minecraft client) {
        Entity target = client.crosshairPickEntity;
        if (target != null) {
            if (target instanceof ItemEntity ie && !ie.getItem().isEmpty()) {
                return new Source(ie.getItem(), 1.0f);
            }
            if (target instanceof ItemFrame frame && !frame.getItem().isEmpty()) {
                return new Source(frame.getItem(), 1.0f);
            }
            if (target instanceof LivingEntity le && !(le instanceof Player)) {
                ItemStack main = le.getMainHandItem();
                ItemStack off = le.getOffhandItem();
                ItemStack best = pickBest(main, off);
                if (best != null && !best.isEmpty()) return new Source(best, 1.0f);
            }
        }

        float alpha = HeldItemTooltipTracker.alpha();
        if (alpha > 0f) {
            Player p = client.player;
            if (p == null) return null;
            ItemStack sel = p.getInventory().getSelectedItem();
            if (sel == null || sel.isEmpty()) return null;
            return new Source(sel, alpha);
        }
        return null;
    }

    private static ItemStack pickBest(ItemStack a, ItemStack b) {
        boolean ae = !effectiveEnchantments(a).isEmpty();
        boolean be = !effectiveEnchantments(b).isEmpty();
        if (ae && be) {
            int ac = effectiveEnchantments(a).size();
            int bc = effectiveEnchantments(b).size();
            return ac >= bc ? a : b;
        }
        if (ae) return a;
        if (be) return b;
        if (!a.isEmpty()) return a;
        return b;
    }

    private static ItemEnchantments effectiveEnchantments(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemEnchantments.EMPTY;
        ItemEnchantments e = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!e.isEmpty()) return e;
        return stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    private static String nameOf(Holder<Enchantment> entry) {
        return entry.unwrapKey()
                .map(k -> prettify(k.identifier().getPath()))
                .orElse("Enchantment");
    }

    private static String prettify(String path) {
        if (path.isEmpty()) return path;
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) sb.append(parts[i].substring(1));
        }
        return sb.toString();
    }

    private static String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> Integer.toString(n);
        };
    }
}
