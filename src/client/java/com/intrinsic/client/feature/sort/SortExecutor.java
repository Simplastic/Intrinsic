package com.intrinsic.client.feature.sort;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;

import java.util.List;

public final class SortExecutor {
    private SortExecutor() {}

    public static void run(AbstractContainerScreen<?> screen, List<ClickOp> ops) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null || mc.player == null) return;
        int syncId = screen.getMenu().containerId;
        for (ClickOp op : ops) {
            mc.gameMode.handleContainerInput(syncId, op.slotId(), 0,
                    ContainerInput.PICKUP, mc.player);
        }
    }
}
