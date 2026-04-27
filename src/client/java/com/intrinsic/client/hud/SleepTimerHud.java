package com.intrinsic.client.hud;

import com.intrinsic.client.feature.Feature;
import com.intrinsic.client.gui.widget.ThemeColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class SleepTimerHud {
    private SleepTimerHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        if (client.player == null || client.level == null) return;

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int x = HudPositions.x("sleepTimer", sw);
        int y = HudPositions.y("sleepTimer", sh);

        String msg;
        int color;
        if (client.player.isSleeping()) {
            long tod = client.level.getOverworldClockTime() % 24000L;
            long dawn = 23460L;
            long ticksLeft = tod >= 12000L ? dawn - tod : 0L;
            if (ticksLeft < 0) ticksLeft = 0;
            float secs = ticksLeft / 20.0f;
            msg = String.format("Dawn in %.1fs", secs);
            color = 0xFFAADFFF;
        } else if (isNight(client.level.getOverworldClockTime())) {
            AABB box = client.player.getBoundingBox().inflate(16);
            EntityTypeTest<Entity, Monster> typeTest = EntityTypeTest.forClass(Monster.class);
            List<Monster> hostiles = client.level.getEntities(typeTest, box, h -> true);
            if (hostiles.isEmpty()) {
                msg = "Safe to sleep";
                color = 0xFF55FF55;
            } else {
                msg = "Hostile nearby (" + hostiles.size() + ")";
                color = 0xFFFF7755;
            }
        } else {
            return;
        }

        int w = client.font.width(msg) + 12;
        HudStyle.card(ctx, x, y, w, 14);
        ctx.text(client.font, Component.literal(msg), x + 6, y + 3, color, true);
    }

    private static boolean isNight(long timeOfDay) {
        long tod = timeOfDay % 24000L;
        return tod >= 13000L && tod <= 23000L;
    }
}
