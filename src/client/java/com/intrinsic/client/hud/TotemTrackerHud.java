package com.intrinsic.client.hud;

import com.intrinsic.client.feature.Feature;
import com.intrinsic.client.gui.widget.ThemeColors;
import com.intrinsic.client.util.ShulkerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;

public final class TotemTrackerHud {
    private TotemTrackerHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        if (client.player == null) return;

        int[] counts = countTotems(client.player.getInventory());
        int direct = counts[0];
        int inShulkers = counts[1];

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int x = HudPositions.x("totemTracker", sw);
        int y = HudPositions.y("totemTracker", sh);

        boolean low = direct == 0 && client.player.getHealth() < 8.0f;

        ItemStack icon = new ItemStack(Items.TOTEM_OF_UNDYING);
        ctx.item(icon, x, y);
        String label = "\u00D7 " + direct;
        int color = direct == 0 ? 0xFFFF5555 : low ? 0xFFFF5555 : ThemeColors.TEXT;
        int textX = x + 20;
        ctx.text(client.font, Component.literal(label), textX, y + 4, color, true);

        int nextX = textX + client.font.width(label);
        if (inShulkers > 0) {
            String shulkerPart = " (+" + inShulkers + ")";
            ctx.text(client.font, Component.literal(shulkerPart), nextX, y + 4, 0xFFAAAAFF, true);
            nextX += client.font.width(shulkerPart);
        }

        if (low) {
            ctx.text(client.font, Component.literal("WARNING"), nextX + 6, y + 4, 0xFFFF5555, true);
        }
    }

    private static int[] countTotems(Inventory inv) {
        int direct = 0;
        int shulker = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            if (s.getItem() == Items.TOTEM_OF_UNDYING) {
                direct += s.getCount();
            } else if (ShulkerUtil.isShulkerBox(s)) {
                for (ItemStack inside : ShulkerUtil.getContents(s)) {
                    if (!inside.isEmpty() && inside.getItem() == Items.TOTEM_OF_UNDYING) {
                        shulker += inside.getCount();
                    }
                }
            }
        }
        return new int[] { direct, shulker };
    }
}
