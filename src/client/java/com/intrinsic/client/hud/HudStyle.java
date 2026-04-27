package com.intrinsic.client.hud;

import com.intrinsic.client.gui.widget.ThemeColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class HudStyle {
    private HudStyle() {}

    public static final int CARD_BG        = 0xCC0B0E14;
    public static final int PAD_X          = 6;
    public static final int PAD_Y          = 3;
    public static final int LINE_STRIDE    = 10;
    public static final int ITEM_STRIDE    = 18;
    public static final int HEADER_H       = 12;
    public static final int WARN_RED       = 0xFFFF5555;
    public static final int OK_GREEN       = 0xFF55FF55;
    public static final int OK_YELLOW      = 0xFFFFFF55;

    public static void textLine(GuiGraphicsExtractor ctx, Font font, String text, int x, int y, int color) {
        ctx.text(font, text, x, y, color, true);
    }

    public static void card(GuiGraphicsExtractor ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, CARD_BG);
        ctx.fill(x, y, x + w, y + 1, ThemeColors.ACCENT);
    }

    public static void cardHeader(GuiGraphicsExtractor ctx, Font font, String title, int x, int y) {
        ctx.text(font, title, x + PAD_X, y + PAD_Y, ThemeColors.TEXT, true);
    }

    public static void cardRow(GuiGraphicsExtractor ctx, Font font, Component text, int x, int y, boolean dimmed) {
        int color = dimmed ? ThemeColors.TEXT_DIM : ThemeColors.TEXT;
        ctx.text(font, text, x + PAD_X, y, color, true);
    }

    public static void warningBanner(GuiGraphicsExtractor ctx, Font font, String text, int anchorX, int anchorY, int anchorW, int color) {
        int tw = font.width(text);
        int x = anchorX + (anchorW - tw) / 2;
        ctx.text(font, text, x, anchorY + PAD_Y, color, true);
    }

    public static boolean blink(int periodMs) {
        return ((System.currentTimeMillis() / periodMs) & 1L) == 0L;
    }
}
