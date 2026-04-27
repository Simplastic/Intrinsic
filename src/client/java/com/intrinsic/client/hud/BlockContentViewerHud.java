package com.intrinsic.client.hud;

import com.intrinsic.client.feature.ContainerContentCache;
import com.intrinsic.client.gui.widget.ThemeColors;
import com.intrinsic.client.util.ShulkerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public final class BlockContentViewerHud {
    private BlockContentViewerHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        ClientLevel level = client.level;
        if (level == null || client.player == null) return;
        HitResult hr = client.hitResult;
        if (!(hr instanceof BlockHitResult bhr) || bhr.getType() == HitResult.Type.MISS) return;

        BlockPos pos = bhr.getBlockPos();
        ContainerContentCache.Snapshot snap = ContainerContentCache.get(level, pos);
        if (snap == null) return;

        List<ItemStack> nonEmpty = new ArrayList<>();
        for (ItemStack s : snap.items) {
            if (s != null && !s.isEmpty()) nonEmpty.add(s);
        }
        if (nonEmpty.isEmpty()) return;

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("blockContent");
        int cardW = a.width;

        int padX = 6;
        int padY = 4;
        int titleH = 12;
        int slotSize = 18;
        int cols = Math.max(1, (cardW - padX * 2) / slotSize);
        int rows = (nonEmpty.size() + cols - 1) / cols;
        int gridW = cols * slotSize;
        int gridH = rows * slotSize;
        int contentHeight = titleH + gridH + padY;

        int x0 = HudPositions.x("blockContent", sw);
        int y0 = HudPositions.y("blockContent", sh);
        int x1 = x0 + cardW;
        int y1 = y0 + contentHeight;

        HudStyle.card(ctx, x0, y0, x1 - x0, y1 - y0);

        String title = (snap.title == null || snap.title.isEmpty()) ? "Contents" : snap.title;
        int tw = client.font.width(title);
        ctx.text(client.font, title, x0 + (cardW - tw) / 2, y0 + 3, ThemeColors.TEXT, true);

        int gridX = x0 + (cardW - gridW) / 2;
        int gridY = y0 + titleH;
        for (int i = 0; i < nonEmpty.size(); i++) {
            ItemStack stack = nonEmpty.get(i);
            int sx = gridX + (i % cols) * slotSize + 1;
            int sy = gridY + (i / cols) * slotSize + 1;
            ctx.item(stack, sx, sy);
            ctx.itemDecorations(client.font, stack, sx, sy);

            if (ShulkerUtil.isShulkerBox(stack)) {
                int count = ShulkerUtil.countContents(stack);
                if (count > 0) {
                    ctx.fill(sx, sy + slotSize - 3, sx + slotSize - 2, sy + slotSize - 2, 0xAA10B981);
                }
            }
        }
    }
}
