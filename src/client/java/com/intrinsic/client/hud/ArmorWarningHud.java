package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ArmorWarningHud {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        if (client.player == null) return;

        int threshold = IntrinsicClient.getConfig().durabilityWarningThreshold;
        String brokenName = null;
        int worstPercent = Integer.MAX_VALUE;

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = client.player.getItemBySlot(slot);
            if (stack.isEmpty() || !stack.isDamageableItem()) continue;
            int maxDamage = stack.getMaxDamage();
            if (maxDamage <= 0) continue;
            int remaining = maxDamage - stack.getDamageValue();
            int percent = remaining * 100 / maxDamage;
            if (percent <= threshold && percent < worstPercent) {
                worstPercent = percent;
                brokenName = stack.getHoverName().getString();
            }
        }

        if (brokenName == null) return;

        long now = System.currentTimeMillis();
        boolean blink = (now / 400) % 2 == 0;
        if (!blink) return;

        String text = "\u26A0 " + brokenName + " at " + worstPercent + "% \u26A0";
        int width = client.font.width(text);

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("armorWarning");
        int x = HudPositions.x("armorWarning", sw) + (a.width - width) / 2;
        int y = HudPositions.y("armorWarning", sh);
        context.text(client.font, text, x, y, 0xFFFF3333, true);
    }
}
