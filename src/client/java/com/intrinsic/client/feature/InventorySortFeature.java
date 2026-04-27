package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.feature.sort.ClickOp;
import com.intrinsic.client.feature.sort.SortExecutor;
import com.intrinsic.client.feature.sort.SortPlanner;
import com.intrinsic.client.feature.sort.SortTarget;
import com.intrinsic.client.feature.sort.SortTargetResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public final class InventorySortFeature {
    private InventorySortFeature() {}

    public static boolean trigger(AbstractContainerScreen<?> screen, Slot focused) {
        if (!IntrinsicClient.getConfig().inventorySort) return false;
        // Creative tab menu uses virtual slots and an infinite item pool — sorting makes no sense.
        if (screen instanceof CreativeModeInventoryScreen) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        // Abort if the player is holding something on the cursor: sorting would lose/misplace it.
        if (!mc.player.containerMenu.getCarried().isEmpty()) return false;

        SortTarget target = SortTargetResolver.resolve(screen, focused);
        if (target == null) return false;

        List<ClickOp> ops = SortPlanner.plan(screen, target);
        if (!ops.isEmpty()) SortExecutor.run(screen, ops);
        return true;
    }
}
