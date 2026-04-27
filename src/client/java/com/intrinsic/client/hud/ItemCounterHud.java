package com.intrinsic.client.hud;

import com.intrinsic.client.util.ShulkerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ItemCounterHud {
    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        if (client.player == null) return;

        ItemStack held = client.player.getMainHandItem();
        if (held.isEmpty()) return;

        Inventory inv = client.player.getInventory();
        int total = 0;
        int inShulkers = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            if (ItemStack.isSameItemSameComponents(s, held)) {
                total += s.getCount();
            } else if (ShulkerUtil.isShulkerBox(s)) {
                for (ItemStack inside : ShulkerUtil.getContents(s)) {
                    if (!inside.isEmpty() && ItemStack.isSameItemSameComponents(inside, held)) {
                        inShulkers += inside.getCount();
                    }
                }
            }
        }

        ItemStack off = client.player.getOffhandItem();
        if (!off.isEmpty() && ItemStack.isSameItemSameComponents(off, held)) {
            total += off.getCount();
        } else if (ShulkerUtil.isShulkerBox(off)) {
            for (ItemStack inside : ShulkerUtil.getContents(off)) {
                if (!inside.isEmpty() && ItemStack.isSameItemSameComponents(inside, held)) {
                    inShulkers += inside.getCount();
                }
            }
        }

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int x = HudPositions.x("itemCounter", sw);
        int y = HudPositions.y("itemCounter", sh);

        int mainColor = 0xFFFFFFFF;
        int shulkerColor = 0xFFAAAAFF;

        // Item icons are 16px; align their vertical center with the 8px-tall text line.
        ctx.item(held, x, y - 4);

        int textX = x + 18;
        String mainPart = String.format("%d", total);
        ctx.text(client.font, mainPart, textX, y, mainColor, true);

        if (inShulkers > 0) {
            int mainWidth = client.font.width(mainPart);
            String shPart = String.format(" (+%d)", inShulkers);
            ctx.text(client.font, shPart, textX + mainWidth, y, shulkerColor, true);
        }
    }
}
