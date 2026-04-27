package com.intrinsic.client.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

// Vanilla BeehiveBlockEntity inherits BlockEntity.getUpdateTag which returns an
// empty CompoundTag — so the client never sees the bee list and
// getOccupantCount() is 0 in the Block Info HUD. BeehiveBlockEntity doesn't
// declare getUpdateTag itself, so we can't @Inject into it; instead we merge a
// new override method onto the subclass via Mixin. The override delegates to
// saveCustomOnly, which includes the `stored` bee list.
//
// Runtime scope: this runs on the integrated server (singleplayer) where the
// mod-loaded class is the one serializing chunks. On dedicated vanilla
// servers this mixin has no effect — BlockInfoHud hides the bee row in that
// case via the Minecraft.hasSingleplayerServer() guard.
@Mixin(BeehiveBlockEntity.class)
public abstract class BeehiveBlockEntityMixin extends BlockEntity {
    private BeehiveBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
