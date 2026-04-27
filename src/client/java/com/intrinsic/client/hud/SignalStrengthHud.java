package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.feature.ContainerContentCache;
import com.intrinsic.client.gui.widget.ThemeColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class SignalStrengthHud {
    private SignalStrengthHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        ClientLevel level = client.level;
        if (level == null || client.player == null) return;
        HitResult hr = client.hitResult;
        if (!(hr instanceof BlockHitResult bhr) || bhr.getType() == HitResult.Type.MISS) return;

        BlockPos pos = bhr.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;

        int best = level.getBestNeighborSignal(pos);
        boolean hasAnalog = state.hasAnalogOutputSignal();
        int analog = hasAnalog ? state.getAnalogOutputSignal(level, pos, bhr.getDirection()) : 0;
        // Containers (chests/barrels/shulkers/hoppers/droppers/dispensers) claim
        // hasAnalogOutputSignal = true but their client-side item list is empty
        // until the player opens them — so the live value reads 0. Fall back to
        // our cached contents (populated by BlockContentViewerHud) so the
        // comparator reading reflects reality.
        boolean analogFromCache = false;
        if (hasAnalog && analog == 0) {
            ContainerContentCache.Snapshot snap = ContainerContentCache.get(level, pos);
            if (snap != null && !snap.items.isEmpty()) {
                SimpleContainer tmp = new SimpleContainer(snap.items.size());
                for (int i = 0; i < snap.items.size(); i++) {
                    ItemStack s = snap.items.get(i);
                    tmp.setItem(i, s == null ? ItemStack.EMPTY : s);
                }
                analog = AbstractContainerMenu.getRedstoneSignalFromContainer(tmp);
                analogFromCache = analog > 0;
            }
        }
        if (best == 0 && !hasAnalog && !isRedstoneComponent(state)) return;
        String title = state.getBlock().getName().getString();

        int padX = 6;
        int padTop = 4;
        int padBottom = 4;
        int titleH = 12;
        int barH = 10;
        int rowH = 10;
        int sectionGap = 4;

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("signalStrength");
        int cardW = a.width;

        int contentHeight = titleH
                + barH + sectionGap
                + rowH * 6
                + (hasAnalog ? sectionGap + barH : 0)
                + padBottom;

        int anchorX = HudPositions.x("signalStrength", sw);
        int anchorY = HudPositions.y("signalStrength", sh);
        int x0 = anchorX;
        int y0 = anchorY;
        int x1 = x0 + cardW;
        int y1 = y0 + padTop + contentHeight;

        HudStyle.card(ctx, x0, y0, x1 - x0, y1 - y0);

        int titleW = client.font.width(title);
        int yy = y0 + padTop;
        ctx.text(client.font, title, x0 + (cardW - titleW) / 2, yy, ThemeColors.TEXT, true);
        yy += titleH;

        drawSegmentedBar(ctx, x0 + padX, yy, cardW - padX * 2, barH, best, 15, signalColor(best));
        String bestLabel = "power " + best + "/15";
        int bestLw = client.font.width(bestLabel);
        ctx.text(client.font, bestLabel, x0 + cardW - padX - bestLw, yy + 1, ThemeColors.TEXT, true);
        yy += barH + sectionGap;

        for (Direction d : Direction.values()) {
            BlockPos neighborPos = pos.relative(d);
            int s = level.getSignal(neighborPos, d);
            boolean direct = level.hasSignal(neighborPos, d);
            String name = d.getName();
            String tag = s > 0 ? (direct ? "direct" : "indirect") : "-";
            String line = String.format("%-6s %2d  %s", name, s, tag);
            int col = s > 0 ? signalColor(s) : ThemeColors.TEXT_FAINT;
            ctx.text(client.font, line, x0 + padX, yy, col, true);
            yy += rowH;
        }

        if (hasAnalog) {
            yy += sectionGap - rowH + barH;
            int aboveY = yy - barH;
            drawSegmentedBar(ctx, x0 + padX, aboveY, cardW - padX * 2, barH, analog, 15, 0xFFD97706);
            String lbl = "comparator " + analog + "/15" + (analogFromCache ? " (cached)" : "");
            int lw = client.font.width(lbl);
            ctx.text(client.font, lbl, x0 + cardW - padX - lw, aboveY + 1, ThemeColors.TEXT, true);
        }
    }

    private static void drawSegmentedBar(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int value, int max, int fillColor) {
        ctx.fill(x, y, x + w, y + h, 0xFF1C1E29);
        int segGap = 1;
        int segW = (w - segGap * (max - 1)) / max;
        int drawX = x;
        for (int i = 0; i < max; i++) {
            int sx0 = drawX;
            int sx1 = drawX + segW;
            int color = i < value ? fillColor : 0xFF2F3345;
            ctx.fill(sx0, y + 1, sx1, y + h - 1, color);
            drawX += segW + segGap;
        }
    }

    private static boolean isRedstoneComponent(BlockState state) {
        Block b = state.getBlock();
        if (b == Blocks.REDSTONE_BLOCK) return true;
        return b instanceof RedStoneWireBlock
                || b instanceof DiodeBlock
                || b instanceof RedstoneTorchBlock
                || b instanceof RedstoneWallTorchBlock
                || b instanceof BasePressurePlateBlock
                || b instanceof ButtonBlock
                || b instanceof LeverBlock
                || b instanceof TripWireHookBlock
                || b instanceof TripWireBlock
                || b instanceof ObserverBlock
                || b instanceof DaylightDetectorBlock
                || b instanceof PistonBaseBlock
                || b instanceof DispenserBlock
                || b instanceof DropperBlock
                || b instanceof RedstoneLampBlock
                || b instanceof PoweredRailBlock
                || b instanceof DetectorRailBlock
                || b instanceof TargetBlock
                || b instanceof LightningRodBlock
                || b instanceof CopperBulbBlock
                || b instanceof CrafterBlock
                || b instanceof SculkSensorBlock
                || b instanceof CalibratedSculkSensorBlock
                || b instanceof NoteBlock;
    }

    private static int signalColor(int v) {
        if (v <= 0) return ThemeColors.TEXT_FAINT;
        if (v >= 12) return 0xFFEF4444;
        if (v >= 7) return 0xFFF59E0B;
        return 0xFF10B981;
    }
}
