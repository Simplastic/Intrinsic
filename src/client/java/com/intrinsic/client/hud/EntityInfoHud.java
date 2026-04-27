package com.intrinsic.client.hud;

import com.intrinsic.client.gui.widget.ThemeColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class EntityInfoHud {
    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        Entity target = client.crosshairPickEntity;
        if (target == null) return;

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("entityInfo");
        int boxX = HudPositions.x("entityInfo", sw);
        int y = HudPositions.y("entityInfo", sh);
        int centerX = boxX + a.width / 2;

        String name = target.getDisplayName().getString();
        String typeTag = classifyTag(target);
        int nameColor = classifyColor(target);
        if (target instanceof AgeableMob am && am.isBaby()) {
            typeTag = typeTag.isEmpty() ? "Baby" : typeTag + " · Baby";
        }
        String header = typeTag.isEmpty() ? name : name + " [" + typeTag + "]";
        drawCentered(context, client, header, centerX, y, nameColor);

        int yy = y + 10;
        if (target instanceof LivingEntity living) {
            float hp = living.getHealth();
            float maxHp = living.getMaxHealth();
            float ratio = hp / Math.max(1.0f, maxHp);
            int color = ratio < 0.33f ? 0xFFFF5555 : ratio < 0.66f ? 0xFFFFFF55 : 0xFF55FF55;
            String hpText = String.format("HP: %.1f / %.1f", hp, maxHp);
            drawCentered(context, client, hpText, centerX, yy, color);
            yy += 10;

            int armor = living.getArmorValue();
            if (armor > 0) {
                drawCentered(context, client, "Armor: " + armor, centerX, yy, ThemeColors.ACCENT);
                yy += 10;
            }

            if (target instanceof AbstractHorse horse) {
                double speed = horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * 43.17;
                drawCentered(context, client, String.format("Speed: %.2f b/s", speed), centerX, yy, ThemeColors.TEXT_DIM);
                yy += 10;

                double jumpStrength = horse.getAttributeValue(Attributes.JUMP_STRENGTH);
                double jumpBlocks = jumpHeight(jumpStrength);
                drawCentered(context, client, String.format("Jump: %.1f blocks", jumpBlocks), centerX, yy, ThemeColors.TEXT_DIM);
                yy += 10;

                if (horse.isSaddled()) {
                    drawCentered(context, client, "Saddled", centerX, yy, ThemeColors.TEXT);
                    yy += 10;
                }

                if (horse.isTamed()) {
                    String ownerLabel = resolveOwnerName(client, horse);
                    drawCentered(context, client, "Tamed" + (ownerLabel != null ? " by " + ownerLabel : ""),
                            centerX, yy, ThemeColors.TEXT);
                    yy += 10;
                }
            }
        }
    }

    private static void drawCentered(GuiGraphicsExtractor ctx, Minecraft client, String text, int centerX, int y, int color) {
        int w = client.font.width(text);
        ctx.text(client.font, text, centerX - w / 2, y, color, true);
    }

    private static double jumpHeight(double strength) {
        return -0.1817584952 * Math.pow(strength, 3.0)
                + 3.689713992 * Math.pow(strength, 2.0)
                + 2.128599134 * strength
                - 0.343930367;
    }

    private static String resolveOwnerName(Minecraft client, AbstractHorse horse) {
        if (client.level == null) return null;
        var ref = horse.getOwnerReference();
        if (ref == null) return null;
        UUID ownerId = ref.getUUID();
        if (ownerId == null) return null;
        Player owner = client.level.getPlayerByUUID(ownerId);
        return owner != null ? owner.getDisplayName().getString() : null;
    }

    private static String classifyTag(Entity e) {
        if (e instanceof Player) return "Player";
        if (e instanceof Monster) return "Hostile";
        if (e instanceof AgeableMob) return "Passive";
        return "";
    }

    private static int classifyColor(Entity e) {
        if (e instanceof Player) return ThemeColors.TEXT;
        if (e instanceof Monster) return 0xFFFF5555;
        if (e instanceof AgeableMob) return 0xFF55FF55;
        return ThemeColors.TEXT;
    }
}
