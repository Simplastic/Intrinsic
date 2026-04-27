package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class DurabilityHud {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.FEET,
            EquipmentSlot.LEGS,
            EquipmentSlot.CHEST,
            EquipmentSlot.HEAD
    };

    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        if (client.player == null) return;

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("durability");
        int anchorX = HudPositions.x("durability", sw);
        int anchorY = HudPositions.y("durability", sh);
        int rightEdge = anchorX + a.width;
        int y = anchorY + a.height - 12;

        ItemStack mainHand = client.player.getMainHandItem();
        if (mainHand.isDamageableItem()) {
            y = renderDurabilityLine(context, client, mainHand, rightEdge, y);
        }

        ItemStack offHand = client.player.getOffhandItem();
        if (offHand.isDamageableItem()) {
            y = renderDurabilityLine(context, client, offHand, rightEdge, y);
        }

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armor = client.player.getItemBySlot(slot);
            if (armor.isDamageableItem()) {
                y = renderDurabilityLine(context, client, armor, rightEdge, y);
            }
        }
    }

    private static int renderDurabilityLine(GuiGraphicsExtractor context, Minecraft client,
                                             ItemStack stack, int rightEdge, int y) {
        int maxDamage = stack.getMaxDamage();
        int currentDamage = stack.getDamageValue();
        int remaining = maxDamage - currentDamage;
        float percent = (float) remaining / maxDamage * 100f;

        int color = percent > 50 ? 0xFF55FF55 : percent > 25 ? 0xFFFFFF55 : 0xFFFF5555;

        String text = String.format("%s: %d%%", stack.getHoverName().getString(), (int) percent);
        int textWidth = client.font.width(text);
        context.text(client.font, text, rightEdge - textWidth, y, color, true);
        return y - 12;
    }
}
