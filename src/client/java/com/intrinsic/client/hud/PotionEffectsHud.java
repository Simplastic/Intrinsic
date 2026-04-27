package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class PotionEffectsHud {
    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        if (client.player == null) return;

        Collection<MobEffectInstance> effects = client.player.getActiveEffects();
        if (effects.isEmpty()) return;

        List<MobEffectInstance> sorted = new ArrayList<>(effects);
        sorted.sort(Comparator.comparingInt(MobEffectInstance::getDuration));

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("potionEffects");
        int x = HudPositions.x("potionEffects", sw);
        int yBase = HudPositions.y("potionEffects", sh) + a.height - 10;
        int i = 0;
        for (MobEffectInstance effect : sorted) {
            if (effect.isInfiniteDuration()) continue;
            int duration = effect.getDuration();
            int color = colorForDuration(duration);
            String name = effect.getEffect().value().getDisplayName().getString();
            int amp = effect.getAmplifier();
            String ampSuffix = amp > 0 ? " " + romanNumeral(amp + 1) : "";
            String text = String.format("%s%s %s", name, ampSuffix, formatDuration(duration));
            int y = yBase - (i * 10);
            context.text(client.font, text, x, y, color, true);
            i++;
            if (i >= 8) break;
        }
    }

    private static int colorForDuration(int ticks) {
        int seconds = ticks / 20;
        if (seconds > 30) return 0xFF55FF55;
        if (seconds > 10) return 0xFFFFFF55;
        return 0xFFFF5555;
    }

    private static String formatDuration(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private static String romanNumeral(int n) {
        if (n < 1 || n > 10) return String.valueOf(n);
        String[] r = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return r[n - 1];
    }
}
