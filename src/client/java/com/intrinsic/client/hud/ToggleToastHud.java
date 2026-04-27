package com.intrinsic.client.hud;

import com.intrinsic.client.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public final class ToggleToastHud {
    private static final long TTL_MS = 1800L;
    private static final long FADE_MS = 400L;
    private static final int LINE_HEIGHT = 12;

    private static final Deque<Toast> toasts = new ArrayDeque<>();

    private ToggleToastHud() {}

    public static void push(Feature f) {
        if (f == null) return;
        push(f.displayName, f.isEnabled());
    }

    public static void push(String label, boolean enabled) {
        toasts.addLast(new Toast(label, enabled, System.currentTimeMillis()));
        while (toasts.size() > 5) toasts.removeFirst();
    }

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        if (toasts.isEmpty()) return;

        long now = System.currentTimeMillis();
        for (Iterator<Toast> it = toasts.iterator(); it.hasNext(); ) {
            Toast t = it.next();
            if (now - t.spawnMs > TTL_MS) it.remove();
        }
        if (toasts.isEmpty()) return;

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("toggleToast");
        int anchorX = HudPositions.x("toggleToast", sw);
        int anchorY = HudPositions.y("toggleToast", sh);

        int i = 0;
        for (Toast t : toasts) {
            long age = now - t.spawnMs;
            float alpha = 1f;
            if (age > TTL_MS - FADE_MS) {
                alpha = Math.max(0f, (TTL_MS - age) / (float) FADE_MS);
            } else if (age < FADE_MS / 2) {
                alpha = Math.min(1f, age / (float) (FADE_MS / 2));
            }
            int a8 = Math.round(alpha * 255f) & 0xFF;
            if (a8 <= 4) { i++; continue; }

            String text = t.label + ": " + (t.enabled ? "ON" : "OFF");
            int tw = client.font.width(text);
            int pad = 4;
            int boxW = tw + pad * 2;
            int boxX = anchorX + a.width / 2 - boxW / 2;
            int boxY = anchorY - i * (LINE_HEIGHT + 2);

            int bg = (a8 << 24) | 0x000000;
            ctx.fill(boxX, boxY, boxX + boxW, boxY + LINE_HEIGHT, bg);
            int accent = (a8 << 24) | (t.enabled ? 0x55FF66 : 0xFF5566);
            ctx.fill(boxX, boxY, boxX + 2, boxY + LINE_HEIGHT, accent);

            int textColor = (a8 << 24) | 0xFFFFFF;
            ctx.text(client.font, text, boxX + pad, boxY + 2, textColor, true);
            i++;
        }
    }

    private static final class Toast {
        final String label;
        final boolean enabled;
        final long spawnMs;

        Toast(String label, boolean enabled, long spawnMs) {
            this.label = label;
            this.enabled = enabled;
            this.spawnMs = spawnMs;
        }
    }
}
