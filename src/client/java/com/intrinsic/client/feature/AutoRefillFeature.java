package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class AutoRefillFeature {
    private AutoRefillFeature() {}

    private static final int OFFHAND_CONTAINER_SLOT = 45;
    private static final int DEADLINE_TICKS = 40;

    private static Pending pendingHotbar = null;
    private static Pending pendingOffhand = null;
    private static int tickCounter = 0;

    public static void register() {
        UseBlockCallback.EVENT.register(AutoRefillFeature::onUseBlock);
        UseItemCallback.EVENT.register(AutoRefillFeature::onUseItem);
        AttackBlockCallback.EVENT.register(AutoRefillFeature::onAttackBlock);
        ClientPlayerBlockBreakEvents.AFTER.register((world, player, pos, state) -> enqueueHeld(InteractionHand.MAIN_HAND));
        ClientTickEvents.END_CLIENT_TICK.register(AutoRefillFeature::onClientTick);
    }

    private static InteractionResult onUseBlock(net.minecraft.world.entity.player.Player player,
                                                 net.minecraft.world.level.Level level,
                                                 InteractionHand hand,
                                                 net.minecraft.world.phys.BlockHitResult hit) {
        enqueueFor(player, hand);
        return InteractionResult.PASS;
    }

    private static InteractionResult onUseItem(net.minecraft.world.entity.player.Player player,
                                                net.minecraft.world.level.Level level,
                                                InteractionHand hand) {
        enqueueFor(player, hand);
        return InteractionResult.PASS;
    }

    private static InteractionResult onAttackBlock(net.minecraft.world.entity.player.Player player,
                                                    net.minecraft.world.level.Level level,
                                                    InteractionHand hand,
                                                    net.minecraft.core.BlockPos pos,
                                                    net.minecraft.core.Direction dir) {
        enqueueFor(player, hand);
        return InteractionResult.PASS;
    }

    private static void enqueueFor(net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        if (!Feature.AUTO_REFILL.isEnabled()) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || player != client.player) return;
        enqueueHeld(hand);
    }

    private static void enqueueHeld(InteractionHand hand) {
        if (!Feature.AUTO_REFILL.isEnabled()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        if (hand == InteractionHand.OFF_HAND) {
            if (!IntrinsicClient.getConfig().autoRefillIncludeOffhand) return;
            ItemStack held = player.getOffhandItem();
            if (held.isEmpty()) return;
            pendingOffhand = new Pending(OFFHAND_CONTAINER_SLOT, held.getItem(), tickCounter + DEADLINE_TICKS);
        } else {
            int selected = player.getInventory().getSelectedSlot();
            ItemStack held = player.getInventory().getItem(selected);
            if (held.isEmpty()) return;
            pendingHotbar = new Pending(selected, held.getItem(), tickCounter + DEADLINE_TICKS);
        }
    }

    private static void onClientTick(Minecraft client) {
        tickCounter++;
        if (pendingHotbar == null && pendingOffhand == null) return;

        LocalPlayer player = client.player;
        if (player == null || client.gameMode == null) {
            pendingHotbar = null;
            pendingOffhand = null;
            return;
        }
        if (client.screen != null || player.isUsingItem()) return;

        pendingHotbar = processEntry(client, player, pendingHotbar, false);
        pendingOffhand = processEntry(client, player, pendingOffhand, true);
    }

    private static Pending processEntry(Minecraft client, LocalPlayer player, Pending entry, boolean isOffhand) {
        if (entry == null) return null;
        Inventory inv = player.getInventory();
        ItemStack current = isOffhand ? player.getOffhandItem() : inv.getItem(entry.slot);

        if (!current.isEmpty()) {
            return tickCounter >= entry.deadlineTick ? null : entry;
        }
        if (entry.item == Items.AIR) return null;

        boolean ok = isOffhand
                ? tryRefillOffhand(client, player, entry.item)
                : tryRefillHotbar(client, player, entry.slot, entry.item);
        if (!ok && tickCounter < entry.deadlineTick) return entry;
        return null;
    }

    private static boolean tryRefillHotbar(Minecraft client, LocalPlayer player, int hotbarSlot, Item wanted) {
        if (!(player.containerMenu instanceof InventoryMenu)) return false;
        int srcInv = findMatchInInventory(player, wanted, player.getInventory().getItem(hotbarSlot), hotbarSlot);
        if (srcInv < 0) return false;
        int containerSlot = invIndexToContainerSlot(srcInv);
        client.gameMode.handleContainerInput(player.containerMenu.containerId, containerSlot, hotbarSlot,
                ContainerInput.SWAP, player);
        return true;
    }

    private static boolean tryRefillOffhand(Minecraft client, LocalPlayer player, Item wanted) {
        if (!(player.containerMenu instanceof InventoryMenu)) return false;
        int srcInv = findMatchInInventory(player, wanted, player.getOffhandItem(), -1);
        if (srcInv < 0) return false;
        int containerSlot = invIndexToContainerSlot(srcInv);
        client.gameMode.handleContainerInput(player.containerMenu.containerId, containerSlot, 40,
                ContainerInput.SWAP, player);
        return true;
    }

    private static int findMatchInInventory(LocalPlayer player, Item wanted, ItemStack reference, int excludeInvIndex) {
        Inventory inv = player.getInventory();
        int best = -1;
        int bestScore = Integer.MIN_VALUE;
        for (int i = 0; i < 36; i++) {
            if (i == excludeInvIndex) continue;
            ItemStack s = inv.getItem(i);
            if (s.isEmpty() || s.getItem() != wanted) continue;
            if (!componentsCompatible(s, reference)) continue;
            int score = s.getCount();
            if (s.isDamageableItem()) score = 10_000 + (s.getMaxDamage() - s.getDamageValue());
            if (score > bestScore) {
                bestScore = score;
                best = i;
            }
        }
        return best;
    }

    private static int invIndexToContainerSlot(int invIndex) {
        return invIndex < 9 ? 36 + invIndex : invIndex;
    }

    private static boolean componentsCompatible(ItemStack candidate, ItemStack reference) {
        if (!IntrinsicClient.getConfig().autoRefillMatchEnchants) return true;
        if (reference == null || reference.isEmpty()) return true;
        ItemEnchantments refE = reference.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments canE = candidate.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        return refE.equals(canE);
    }

    private record Pending(int slot, Item item, int deadlineTick) {}
}
