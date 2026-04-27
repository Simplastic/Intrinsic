package com.intrinsic.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public final class IntSlider {
    public int x, y, w, h;
    public int value;
    public final int min;
    public final int max;
    public boolean dragging;
    private final String unitSuffix;
    private static final int LABEL_W = 58;

    public IntSlider(int x, int y, int w, int h, int value, int min, int max, String unitSuffix) {
        this.x = x; this.y = y; this.w = w; this.h = h;
        this.min = min; this.max = max;
        this.unitSuffix = unitSuffix == null ? "" : unitSuffix;
        this.value = clamp(value);
    }

    private int clamp(int v) {
        return Math.max(min, Math.min(max, v));
    }

    public boolean contains(double mx, double my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public void render(GuiGraphicsExtractor ctx, Font tr, int mouseX, int mouseY, boolean enabled) {
        int trackY = y + h / 2 - 2;
        int trackH = 4;
        int trackW = w - LABEL_W - 6;
        int trackX = x;

        int bgTrack = enabled ? ThemeColors.CHIP : ThemeColors.OFF_DARK;
        ctx.fill(trackX, trackY, trackX + trackW, trackY + trackH, bgTrack);

        double t = (double) (value - min) / (max - min);
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        int fillW = (int) (trackW * t);
        int fill = enabled ? ThemeColors.ACCENT : ThemeColors.OFF;
        ctx.fill(trackX, trackY, trackX + fillW, trackY + trackH, fill);

        int thumbX = trackX + fillW - 3;
        int thumbY = y + 2;
        int thumbW = 6;
        int thumbH = h - 4;
        boolean hoverThumb = mouseX >= thumbX && mouseX <= thumbX + thumbW
                && mouseY >= thumbY && mouseY <= thumbY + thumbH;
        int thumbColor = !enabled ? ThemeColors.TEXT_FAINT
                : (hoverThumb || dragging ? 0xFFFFFFFF : ThemeColors.TEXT);
        ctx.fill(thumbX, thumbY, thumbX + thumbW, thumbY + thumbH, thumbColor);
        ctx.fill(thumbX, thumbY, thumbX + thumbW, thumbY + 1, ThemeColors.DIVIDER);
        ctx.fill(thumbX, thumbY + thumbH - 1, thumbX + thumbW, thumbY + thumbH, ThemeColors.DIVIDER);

        String label = value + unitSuffix;
        int tw = tr.width(label);
        int tx = x + w - tw;
        int ty = y + (h - 8) / 2;
        int textColor = enabled ? ThemeColors.TEXT : ThemeColors.TEXT_FAINT;
        ctx.text(tr, Component.literal(label).withStyle(enabled ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY), tx, ty, textColor, true);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return false;
        int trackW = w - LABEL_W - 6;
        if (mx < x || mx > x + trackW || my < y || my > y + h) return false;
        dragging = true;
        updateFromMouseX(mx);
        return true;
    }

    public boolean mouseDragged(double mx, double my, int button) {
        if (!dragging || button != 0) return false;
        updateFromMouseX(mx);
        return true;
    }

    public boolean mouseReleased(double mx, double my, int button) {
        if (button != 0) return false;
        boolean wasDragging = dragging;
        dragging = false;
        return wasDragging;
    }

    public boolean mouseScrolled(double mx, double my, double vertical, boolean fine) {
        if (mx < x || mx > x + w || my < y || my > y + h) return false;
        int step = fine ? 1 : 2;
        value = clamp(value + (int) Math.signum(vertical) * step);
        return true;
    }

    private void updateFromMouseX(double mx) {
        int trackW = w - LABEL_W - 6;
        double t = (mx - x) / trackW;
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        value = clamp((int) Math.round(min + t * (max - min)));
    }
}
