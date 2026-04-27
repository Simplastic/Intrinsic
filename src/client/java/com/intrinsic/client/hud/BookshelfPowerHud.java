package com.intrinsic.client.hud;

import com.intrinsic.client.gui.widget.ThemeColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class BookshelfPowerHud {
    private BookshelfPowerHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        ClientLevel level = client.level;
        if (level == null || client.player == null) return;
        HitResult hr = client.hitResult;
        if (!(hr instanceof BlockHitResult bhr) || bhr.getType() == HitResult.Type.MISS) return;

        BlockPos tablePos = bhr.getBlockPos();
        BlockState state = level.getBlockState(tablePos);
        if (!state.is(Blocks.ENCHANTING_TABLE)) return;

        int count = 0;
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (EnchantingTableBlock.isValidBookShelf(level, tablePos, offset)) {
                count++;
                if (count >= 15) break;
            }
        }

        String title = "Enchanting Table";
        String row = "Bookshelves: " + count + "/15";
        int rowColor = count >= 15 ? HudStyle.OK_GREEN
                : count == 0 ? HudStyle.WARN_RED
                : HudStyle.OK_YELLOW;

        int padX = 6;
        int titleH = 12;
        int rowH = 10;
        int cardH = titleH + rowH + 4;
        int titleW = client.font.width(title);
        int rowW = client.font.width(row);
        int cardW = Math.min(Math.max(titleW, rowW) + padX * 2, 200);

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("bookshelfPower");
        int anchorX = HudPositions.x("bookshelfPower", sw);
        int anchorY = HudPositions.y("bookshelfPower", sh);
        int x0 = anchorX + a.width / 2 - cardW / 2;
        int y0 = anchorY;

        HudStyle.card(ctx, x0, y0, cardW, cardH);

        int yy = y0 + 3;
        ctx.text(client.font, title, x0 + (cardW - titleW) / 2, yy, ThemeColors.TEXT, true);
        yy += titleH;
        ctx.text(client.font, row, x0 + (cardW - rowW) / 2, yy, rowColor, true);
    }
}
