package com.intrinsic.client.feature;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;

import java.util.Optional;

public final class ArmorSwapFeature {
    // Container slot indices for armor in the player inventory menu.
    private static final int HELMET_SLOT = 5;
    private static final int CHEST_SLOT = 6;
    private static final int LEGS_SLOT = 7;
    private static final int BOOTS_SLOT = 8;

    // Per-level weights for enchants that apply on any armor slot.
    // Generic Protection is worth ~4% damage reduction per level; specialised
    // protections apply only to one damage family, so they weigh less.
    // Mending is flat (level-independent) — it's a binary "has it / doesn't".
    private static final int W_PROTECTION          = 8;
    private static final int W_SPECIAL_PROTECTION  = 3;
    private static final int W_UNBREAKING          = 6;
    private static final int W_THORNS              = 2;
    private static final int W_MENDING_FLAT        = 20;

    // Slot-specific enchantment weights (per level).
    private static final int W_FEATHER_FALLING     = 5;  // boots
    private static final int W_DEPTH_STRIDER       = 2;  // boots
    private static final int W_SOUL_SPEED          = 2;  // boots
    private static final int W_RESPIRATION         = 4;  // helmet
    private static final int W_AQUA_AFFINITY       = 6;  // helmet (flat, but levels are always 1)

    private static int cooldownTicks = 0;

    private ArmorSwapFeature() {}

    public static void tick(Minecraft client) {
        if (cooldownTicks > 0) cooldownTicks--;
        if (!Feature.ARMOR_SWAP.isEnabled()) return;
        LocalPlayer player = client.player;
        if (player == null || client.gameMode == null) return;
        if (!(player.containerMenu instanceof InventoryMenu)) return;
        if (cooldownTicks > 0) return;

        if (tryUpgradeSlot(client, player, EquipmentSlot.HEAD, HELMET_SLOT)) { cooldownTicks = 5; return; }
        if (tryUpgradeSlot(client, player, EquipmentSlot.CHEST, CHEST_SLOT)) { cooldownTicks = 5; return; }
        if (tryUpgradeSlot(client, player, EquipmentSlot.LEGS, LEGS_SLOT))   { cooldownTicks = 5; return; }
        if (tryUpgradeSlot(client, player, EquipmentSlot.FEET, BOOTS_SLOT))  { cooldownTicks = 5; }
    }

    private static boolean tryUpgradeSlot(Minecraft client, LocalPlayer player,
                                          EquipmentSlot slot, int armorMenuSlot) {
        ItemStack worn = player.getItemBySlot(slot);
        Inventory inv = player.getInventory();
        int bestInvIdx = -1;
        int bestScore = score(worn, slot);
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty() || !fitsSlot(s, slot)) continue;
            int sc = score(s, slot);
            if (sc > bestScore) {
                bestScore = sc;
                bestInvIdx = i;
            }
        }
        if (bestInvIdx < 0) return false;

        // ContainerInput.SWAP only accepts button ∈ [0,8] (hotbar) or 40 (offhand) —
        // armor slots are not reachable via SWAP. Use a three-PICKUP dance instead:
        //   1. Pick up the new piece from inventory → cursor.
        //   2. Pick up on the armor slot → swap (or deposit if slot empty).
        //   3. Pick up on the (now empty) inventory slot → deposit the old piece.
        int srcMenuSlot = bestInvIdx < 9 ? 36 + bestInvIdx : bestInvIdx;
        int containerId = player.containerMenu.containerId;
        client.gameMode.handleContainerInput(containerId, srcMenuSlot, 0, ContainerInput.PICKUP, player);
        client.gameMode.handleContainerInput(containerId, armorMenuSlot, 0, ContainerInput.PICKUP, player);
        client.gameMode.handleContainerInput(containerId, srcMenuSlot, 0, ContainerInput.PICKUP, player);
        return true;
    }

    private static boolean fitsSlot(ItemStack stack, EquipmentSlot slot) {
        Equippable eq = stack.get(DataComponents.EQUIPPABLE);
        return eq != null && eq.slot() == slot;
    }

    private static int score(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) return -1;
        Equippable eq = stack.get(DataComponents.EQUIPPABLE);
        if (eq == null || eq.slot() != slot) return -1;

        ItemAttributeModifiers mods = stack.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        double armor = mods.compute(Attributes.ARMOR, 0.0, slot);
        double toughness = mods.compute(Attributes.ARMOR_TOUGHNESS, 0.0, slot);
        int score = (int) (armor * 100 + toughness * 10);

        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchants.entrySet()) {
            Optional<ResourceKey<Enchantment>> keyOpt = entry.getKey().unwrapKey();
            if (keyOpt.isEmpty()) continue;
            score += weightFor(keyOpt.get(), entry.getIntValue(), slot);
        }
        return score;
    }

    private static int weightFor(ResourceKey<Enchantment> key, int level, EquipmentSlot slot) {
        // Universal (any-slot) enchants.
        if (key == Enchantments.PROTECTION)            return level * W_PROTECTION;
        if (key == Enchantments.PROJECTILE_PROTECTION) return level * W_SPECIAL_PROTECTION;
        if (key == Enchantments.BLAST_PROTECTION)      return level * W_SPECIAL_PROTECTION;
        if (key == Enchantments.FIRE_PROTECTION)       return level * W_SPECIAL_PROTECTION;
        if (key == Enchantments.UNBREAKING)            return level * W_UNBREAKING;
        if (key == Enchantments.THORNS)                return level * W_THORNS;
        if (key == Enchantments.MENDING)               return W_MENDING_FLAT;

        // Slot-specific enchants.
        if (slot == EquipmentSlot.FEET) {
            if (key == Enchantments.FEATHER_FALLING) return level * W_FEATHER_FALLING;
            if (key == Enchantments.DEPTH_STRIDER)   return level * W_DEPTH_STRIDER;
            if (key == Enchantments.SOUL_SPEED)      return level * W_SOUL_SPEED;
        } else if (slot == EquipmentSlot.HEAD) {
            if (key == Enchantments.RESPIRATION)   return level * W_RESPIRATION;
            if (key == Enchantments.AQUA_AFFINITY) return W_AQUA_AFFINITY;
        }
        return 0;
    }
}
