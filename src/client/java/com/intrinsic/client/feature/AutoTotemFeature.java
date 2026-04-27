package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AutoTotemFeature {
    // SWAP key for offhand-target is encoded as 40 in container packets,
    // matching AutoRefillFeature's offhand swap.
    private static final int OFFHAND_HOTBAR_KEY = 40;

    private static int cooldownTicks = 0;

    private AutoTotemFeature() {}

    public static void tick(Minecraft client) {
        if (cooldownTicks > 0) cooldownTicks--;
        if (!Feature.AUTO_TOTEM.isEnabled()) return;
        LocalPlayer player = client.player;
        if (player == null || client.gameMode == null) return;
        if (!(player.containerMenu instanceof InventoryMenu)) return;

        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        float threshold = (float) Math.max(1.0, Math.min(20.0, cfg.autoTotemThreshold));
        if (player.getHealth() > threshold) return;
        if (player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;
        if (cooldownTicks > 0) return;

        Inventory inv = player.getInventory();
        int srcInv = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.is(Items.TOTEM_OF_UNDYING)) {
                srcInv = i;
                break;
            }
        }
        if (srcInv < 0) return;

        int containerSlot = srcInv < 9 ? 36 + srcInv : srcInv;
        client.gameMode.handleContainerInput(player.containerMenu.containerId,
                containerSlot, OFFHAND_HOTBAR_KEY, ContainerInput.SWAP, player);
        // Throttle: avoid hammering the swap until the inventory state syncs.
        cooldownTicks = 5;
    }
}
