package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.hud.HudPositions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;

public final class ElytraAlertFeature {
    private static boolean alertFiredThisFlight = false;
    private static long lastAlertMs = 0;
    private static boolean showBanner = false;

    private ElytraAlertFeature() {}

    public static void tick(Minecraft client) {
        if (!Feature.ELYTRA_ALERT.isEnabled()) {
            showBanner = false;
            return;
        }
        if (client.player == null) {
            showBanner = false;
            return;
        }
        boolean gliding = client.player.isFallFlying();
        if (!gliding) {
            alertFiredThisFlight = false;
            if (System.currentTimeMillis() - lastAlertMs > 4000) showBanner = false;
            return;
        }
        if (alertFiredThisFlight) return;

        ItemStack chest = client.player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() != Items.ELYTRA || !chest.isDamageableItem()) return;

        int remaining = chest.getMaxDamage() - chest.getDamageValue();
        int threshold = IntrinsicClient.getConfig().elytraAlertThreshold;
        if (remaining <= threshold) {
            alertFiredThisFlight = true;
            lastAlertMs = System.currentTimeMillis();
            showBanner = true;
        }
    }

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        if (!showBanner) return;
        if (client.player == null) return;
        if (System.currentTimeMillis() - lastAlertMs > 4000) {
            showBanner = false;
            return;
        }

        ItemStack chest = client.player.getItemBySlot(EquipmentSlot.CHEST);
        int remaining = chest.getItem() == Items.ELYTRA ? chest.getMaxDamage() - chest.getDamageValue() : 0;

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int x = HudPositions.x("elytraAlert", sw);
        int y = HudPositions.y("elytraAlert", sh);

        String msg = "ELYTRA LOW: " + remaining + " uses left";
        int w = client.font.width(msg) + 12;
        ctx.fill(x, y, x + w, y + 14, 0xCC3A0F0F);
        ctx.fill(x, y, x + w, y + 1, 0xFFFF5555);
        ctx.text(client.font, Component.literal(msg), x + 6, y + 3, 0xFFFFAAAA, true);
    }
}
