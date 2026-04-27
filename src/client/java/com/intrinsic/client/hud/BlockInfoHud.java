package com.intrinsic.client.hud;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.gui.widget.ThemeColors;
import com.intrinsic.client.mixin.AbstractFurnaceAccessor;
import com.intrinsic.client.mixin.BrewingStandBlockEntityAccessor;
import com.intrinsic.client.mixin.HopperBlockEntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public final class BlockInfoHud {
    private BlockInfoHud() {}

    public static void render(GuiGraphicsExtractor ctx, Minecraft client) {
        ClientLevel level = client.level;
        if (level == null || client.player == null) return;
        HitResult hr = client.hitResult;
        if (!(hr instanceof BlockHitResult bhr) || bhr.getType() == HitResult.Type.MISS) return;

        BlockPos pos = bhr.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;

        String name = state.getBlock().getName().getString();
        List<String> rows = new ArrayList<>();
        describe(state, level.getBlockEntity(pos), rows);
        if (rows.isEmpty()) return;

        int padX = 6;
        int padY = 4;
        int rowH = 10;
        int titleH = 12;
        int contentHeight = titleH + rows.size() * rowH + padY;
        int titleWidth = client.font.width(name);
        int contentWidth = titleWidth;
        for (String r : rows) {
            contentWidth = Math.max(contentWidth, client.font.width(r));
        }
        int cardW = Math.min(contentWidth + padX * 2, 260);

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        HudPositions.Anchor a = HudPositions.get("blockInfo");
        int anchorX = HudPositions.x("blockInfo", sw);
        int anchorY = HudPositions.y("blockInfo", sh);
        int x0 = anchorX + a.width / 2 - cardW / 2;
        int y0 = anchorY;
        int x1 = x0 + cardW;
        int y1 = y0 + contentHeight;

        HudStyle.card(ctx, x0, y0, x1 - x0, y1 - y0);

        int yy = y0 + 3;
        ctx.text(client.font, name, x0 + (cardW - titleWidth) / 2, yy, ThemeColors.TEXT, true);
        yy += titleH;
        for (String r : rows) {
            int tw = client.font.width(r);
            ctx.text(client.font, r, x0 + (cardW - tw) / 2, yy, ThemeColors.TEXT_DIM, true);
            yy += rowH;
        }
    }

    private static void describe(BlockState state, BlockEntity be, List<String> rows) {
        if (state.getBlock() instanceof BeehiveBlock) {
            int honey = state.getValue(BeehiveBlock.HONEY_LEVEL);
            rows.add("honey: " + honey + "/5");
            if (be instanceof BeehiveBlockEntity hive) {
                // On vanilla servers the client-side `stored` list is empty because
                // BeehiveBlockEntity doesn't override getUpdateTag — only show the
                // row when we actually have data (count > 0) or on singleplayer
                // where our mixin forces a full sync.
                int count = hive.getOccupantCount();
                if (count > 0 || Minecraft.getInstance().hasSingleplayerServer()) {
                    rows.add("bees: " + count + "/3");
                }
            }
            return;
        }
        if (state.getBlock() instanceof ComposterBlock) {
            rows.add("level: " + state.getValue(ComposterBlock.LEVEL) + "/8");
            return;
        }
        if (state.getBlock() instanceof LayeredCauldronBlock) {
            rows.add("level: " + state.getValue(LayeredCauldronBlock.LEVEL) + "/3");
            return;
        }
        if (state.getBlock() instanceof RespawnAnchorBlock) {
            rows.add("charges: " + state.getValue(RespawnAnchorBlock.CHARGE) + "/4");
            return;
        }
        if (state.getBlock() instanceof CampfireBlock) {
            rows.add(state.getValue(CampfireBlock.LIT) ? "lit" : "unlit");
            return;
        }
        if (state.getBlock() instanceof CandleBlock) {
            int n = state.getValue(CandleBlock.CANDLES);
            boolean lit = state.getValue(CandleBlock.LIT);
            rows.add("candles: " + n + (lit ? " (lit)" : " (unlit)"));
            return;
        }
        if (state.getBlock() instanceof LecternBlock) {
            rows.add(state.getValue(LecternBlock.HAS_BOOK) ? "book: yes" : "book: no");
            return;
        }
        if (state.getBlock() instanceof FarmlandBlock) {
            rows.add("moisture: " + state.getValue(FarmlandBlock.MOISTURE) + "/7");
            return;
        }
        if (state.getBlock() instanceof CropBlock crop) {
            rows.add("growth: " + crop.getAge(state) + "/" + crop.getMaxAge());
            return;
        }
        if (state.getBlock() instanceof StemBlock) {
            rows.add("growth: " + state.getValue(StemBlock.AGE) + "/7");
            return;
        }
        if (state.getBlock() instanceof AbstractFurnaceBlock) {
            boolean lit = state.getValue(AbstractFurnaceBlock.LIT);
            rows.add(lit ? "burning" : "idle");
            if (be instanceof AbstractFurnaceBlockEntity furnace) {
                int cooked = ((AbstractFurnaceAccessor) furnace).intrinsic_cookTime();
                int total = ((AbstractFurnaceAccessor) furnace).intrinsic_cookTimeTotal();
                if (total > 0) rows.add("cook: " + cooked + "/" + total);
            }
            return;
        }
        if (be instanceof BrewingStandBlockEntity stand) {
            int brew = ((BrewingStandBlockEntityAccessor) stand).intrinsic_brewTime();
            int fuel = ((BrewingStandBlockEntityAccessor) stand).intrinsic_fuel();
            rows.add("brew: " + brew + "/400");
            rows.add("fuel: " + fuel + "/20");
            return;
        }
        if (be instanceof ConduitBlockEntity conduit) {
            rows.add(conduit.isActive() ? "active" : "inactive");
            return;
        }
        if (be instanceof BellBlockEntity bell) {
            rows.add(bell.shaking ? "ringing" : "idle");
            return;
        }
        if (be instanceof JukeboxBlockEntity jb) {
            rows.add(jb.getSongPlayer() != null && jb.getSongPlayer().isPlaying() ? "playing" : "stopped");
            return;
        }
        if (state.getBlock() instanceof SculkSensorBlock) {
            SculkSensorPhase phase = state.getValue(SculkSensorBlock.PHASE);
            int freq = state.getValue(SculkSensorBlock.POWER);
            rows.add("phase: " + phase.name().toLowerCase());
            rows.add("power: " + freq + "/15");
            return;
        }
        if (state.getBlock() instanceof NoteBlock) {
            int note = state.getValue(NoteBlock.NOTE);
            NoteBlockInstrument instrument = state.getValue(NoteBlock.INSTRUMENT);
            boolean powered = state.getValue(NoteBlock.POWERED);
            rows.add("note: " + note + "/24");
            rows.add("instrument: " + instrument.getSerializedName());
            if (powered) rows.add("powered");
            return;
        }
        if (state.getBlock() instanceof PistonBaseBlock) {
            rows.add(state.getValue(PistonBaseBlock.EXTENDED) ? "extended" : "retracted");
            return;
        }
        if (state.getBlock() instanceof HopperBlock) {
            boolean enabled = state.getValue(HopperBlock.ENABLED);
            rows.add(enabled ? "enabled" : "locked");
            if (be instanceof HopperBlockEntity hopper) {
                int cd = ((HopperBlockEntityAccessor) hopper).intrinsic_cooldownTime();
                if (cd > 0) rows.add("cooldown: " + cd);
            }
            return;
        }
        if (state.getBlock() instanceof DaylightDetectorBlock) {
            rows.add(state.getValue(DaylightDetectorBlock.INVERTED) ? "inverted" : "normal");
            rows.add("power: " + state.getValue(DaylightDetectorBlock.POWER) + "/15");
            return;
        }
    }
}
