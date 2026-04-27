package com.intrinsic.client.hud;

import com.intrinsic.client.gui.widget.ThemeColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class MobHealthBarHud {
    private MobHealthBarHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        Entity target = client.crosshairPickEntity;
        if (!(target instanceof LivingEntity le) || target instanceof Player) return;
        if (!le.isAlive()) return;

        float max = le.getMaxHealth();
        float cur = le.getHealth();
        if (max <= 0) return;
        float frac = Math.max(0f, Math.min(1f, cur / max));

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("mobHealthBar");
        int cardW = a.width;

        int padX = 6;
        int nameH = 10;
        int barH = 5;
        int labelH = 10;
        int padY = 4;
        int contentHeight = nameH + 2 + barH + 2 + labelH + padY;

        int x0 = HudPositions.x("mobHealthBar", sw);
        int y0 = HudPositions.y("mobHealthBar", sh);
        int x1 = x0 + cardW;
        int y1 = y0 + contentHeight;

        HudStyle.card(ctx, x0, y0, x1 - x0, y1 - y0);

        Component name = le.getDisplayName() != null ? le.getDisplayName() : Component.literal(le.getType().getDescriptionId());
        int nw = client.font.width(name);
        ctx.text(client.font, name, x0 + (cardW - nw) / 2, y0 + 2, ThemeColors.TEXT, true);

        int barX = x0 + padX;
        int barY = y0 + nameH + 2;
        int barW = cardW - padX * 2;
        ctx.fill(barX, barY, barX + barW, barY + barH, 0xFF2F3345);
        int filled = Math.round(barW * frac);
        ctx.fill(barX, barY, barX + filled, barY + barH, healthColor(frac));

        int hearts = (int) Math.ceil(cur / 2f);
        int maxHearts = (int) Math.ceil(max / 2f);
        String label = String.format("%.1f / %.1f  (%d/%d \u2764)", cur, max, hearts, maxHearts);
        int lw = client.font.width(label);
        ctx.text(client.font, label, x0 + (cardW - lw) / 2, barY + barH + 2, ThemeColors.TEXT_DIM, true);
    }

    private static int healthColor(float frac) {
        if (frac > 0.66f) return 0xFF10B981;
        if (frac > 0.33f) return 0xFFF59E0B;
        return 0xFFEF4444;
    }
}
