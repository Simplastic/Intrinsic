package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import com.intrinsic.client.hud.HudPositions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Util;

public class DurabilityWarningFeature {
    private static boolean showWarning = false;
    private static String warningText = "";
    private static int warningPercent = 100;

    public static void tick(Minecraft client) {
        if (!IntrinsicClient.getConfig().durabilityWarning) {
            showWarning = false;
            return;
        }
        if (client.player == null) {
            showWarning = false;
            return;
        }

        ItemStack held = client.player.getMainHandItem();
        if (held.isEmpty() || !held.isDamageableItem()) {
            showWarning = false;
            return;
        }

        IntrinsicConfig config = IntrinsicClient.getConfig();
        int maxDamage = held.getMaxDamage();
        int remaining = maxDamage - held.getDamageValue();
        float percent = (float) remaining / maxDamage * 100f;

        if (percent <= config.durabilityWarningThreshold) {
            showWarning = true;
            warningPercent = (int) percent;
            warningText = held.getHoverName().getString();
        } else {
            showWarning = false;
        }
    }

    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        if (!showWarning) return;

        // Blink effect: visible half the time
        long time = Util.getMillis();
        boolean flash = (time / 500) % 2 == 0;

        String title = "⚠ LOW DURABILITY ⚠";
        String detail = String.format("%s: %d%% — about to break!", warningText, warningPercent);

        int tw = client.font.width(title);
        int dw = client.font.width(detail);
        int boxW = Math.max(tw, dw) + 12;
        int boxH = 26;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int anchorX = HudPositions.x("durabilityWarning", screenWidth);
        int anchorY = HudPositions.y("durabilityWarning", screenHeight);
        HudPositions.Anchor anchor = HudPositions.get("durabilityWarning");
        int x = anchorX + (anchor.width - boxW) / 2;
        int y = anchorY;

        int bgColor = flash ? 0xCCFF0000 : 0x88AA0000;
        int borderColor = flash ? 0xFFFFFF00 : 0xFFAA5500;
        context.fill(x, y, x + boxW, y + boxH, bgColor);
        context.fill(x, y, x + boxW, y + 1, borderColor);
        context.fill(x, y + boxH - 1, x + boxW, y + boxH, borderColor);
        context.fill(x, y, x + 1, y + boxH, borderColor);
        context.fill(x + boxW - 1, y, x + boxW, y + boxH, borderColor);

        int titleX = x + (boxW - tw) / 2;
        int detailX = x + (boxW - dw) / 2;
        context.text(client.font, title, titleX, y + 4, 0xFFFFFF55, true);
        context.text(client.font, detail, detailX, y + 15, 0xFFFFFFFF, true);
    }
}
