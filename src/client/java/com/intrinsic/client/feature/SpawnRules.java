package com.intrinsic.client.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class SpawnRules {
    private SpawnRules() {}

    public static boolean isSpawnable(Level world, BlockPos cell,
                                       BlockPos.MutableBlockPos below, BlockPos.MutableBlockPos above) {
        BlockState here = world.getBlockState(cell);
        if (!here.isAir()) return false;
        below.set(cell.getX(), cell.getY() - 1, cell.getZ());
        BlockState floor = world.getBlockState(below);
        if (!floor.isValidSpawn(world, below, EntityType.ZOMBIE)) return false;
        if (!floor.getFluidState().isEmpty()) return false;
        above.set(cell.getX(), cell.getY() + 1, cell.getZ());
        BlockState up = world.getBlockState(above);
        return up.isAir();
    }

    public static boolean isSpawnable(BlockGetter view, BlockPos cell) {
        BlockState here = view.getBlockState(cell);
        if (!here.isAir()) return false;
        BlockPos belowPos = cell.below();
        BlockState floor = view.getBlockState(belowPos);
        if (!floor.isValidSpawn(view, belowPos, EntityType.ZOMBIE)) return false;
        if (!floor.getFluidState().isEmpty()) return false;
        return view.getBlockState(cell.above()).isAir();
    }
}
