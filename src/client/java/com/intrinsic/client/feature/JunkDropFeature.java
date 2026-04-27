package com.intrinsic.client.feature;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class JunkDropFeature {
    private JunkDropFeature() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("ijunk")
                    .then(ClientCommands.literal("add")
                            .then(ClientCommands.argument("item", StringArgumentType.greedyString())
                                    .executes(c -> addItem(c, StringArgumentType.getString(c, "item")))))
                    .then(ClientCommands.literal("remove")
                            .then(ClientCommands.argument("item", StringArgumentType.greedyString())
                                    .executes(c -> removeItem(c, StringArgumentType.getString(c, "item")))))
                    .then(ClientCommands.literal("clear").executes(JunkDropFeature::clearList))
                    .executes(JunkDropFeature::listItems));
        });
    }

    /** Hotkey trigger entry point. Respects the Junk Drop master toggle. */
    public static void dropAll(Minecraft client) {
        if (!Feature.JUNK_DROP.isEnabled()) return;
        dropAllUnchecked(client);
    }

    private static void dropAllUnchecked(Minecraft client) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        LocalPlayer player = client.player;
        if (player == null || client.gameMode == null) return;
        if (!(player.containerMenu instanceof InventoryMenu)) return;
        if (cfg.junkBlacklist.isEmpty()) {
            player.sendSystemMessage(Component.literal("Junk blacklist empty. Add with /ijunk add <item>.")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }

        Inventory inv = player.getInventory();
        int dropped = 0;
        int containerId = player.containerMenu.containerId;
        // In creative the container THROW path also triggers a creative-slot
        // resync that the server interprets as a second drop — duplicating the
        // stack on the ground. Use the dedicated creative drop helper instead.
        boolean creative = player.hasInfiniteMaterials();
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (!matches(stack, cfg.junkBlacklist)) continue;
            int count = stack.getCount();
            if (creative) {
                client.gameMode.handleCreativeModeItemDrop(stack.copy());
                inv.setItem(i, ItemStack.EMPTY);
            } else {
                int slot = i < 9 ? 36 + i : i;
                client.gameMode.handleContainerInput(containerId, slot, 1, ContainerInput.THROW, player);
            }
            dropped += count;
        }
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && matches(offhand, cfg.junkBlacklist)) {
            int count = offhand.getCount();
            if (creative) {
                client.gameMode.handleCreativeModeItemDrop(offhand.copy());
                player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            } else {
                client.gameMode.handleContainerInput(containerId, 45, 1, ContainerInput.THROW, player);
            }
            dropped += count;
        }

        if (dropped > 0) {
            player.sendSystemMessage(Component.literal("Dropped " + dropped + " junk items.")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static boolean matches(ItemStack stack, java.util.List<String> blacklist) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return blacklist.contains(id);
    }

    private static int addItem(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> c, String raw) {
        String id = canonicalize(raw);
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
        if (item == null || BuiltInRegistries.ITEM.getKey(item).toString().equals("minecraft:air")) {
            c.getSource().sendFeedback(Component.literal("Unknown item: " + raw).withStyle(ChatFormatting.RED));
            return 0;
        }
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        if (cfg.junkBlacklist.contains(id)) {
            c.getSource().sendFeedback(Component.literal(id + " already in the list.").withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        cfg.junkBlacklist.add(id);
        cfg.save();
        c.getSource().sendFeedback(Component.literal("Added " + id + " to junk blacklist.").withStyle(ChatFormatting.AQUA));
        return 1;
    }

    private static int removeItem(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> c, String raw) {
        String id = canonicalize(raw);
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        boolean removed = cfg.junkBlacklist.remove(id);
        if (removed) cfg.save();
        c.getSource().sendFeedback(Component.literal(removed
                ? "Removed " + id + " from junk blacklist."
                : id + " was not in the list.").withStyle(removed ? ChatFormatting.YELLOW : ChatFormatting.RED));
        return removed ? 1 : 0;
    }

    private static int clearList(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> c) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        int n = cfg.junkBlacklist.size();
        cfg.junkBlacklist.clear();
        cfg.save();
        c.getSource().sendFeedback(Component.literal("Cleared " + n + " entries.").withStyle(ChatFormatting.YELLOW));
        return 1;
    }

    private static int listItems(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> c) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        if (cfg.junkBlacklist.isEmpty()) {
            c.getSource().sendFeedback(Component.literal("Junk blacklist is empty. Add with /ijunk add <item>.").withStyle(ChatFormatting.GRAY));
            return 0;
        }
        c.getSource().sendFeedback(Component.literal("Junk blacklist (" + cfg.junkBlacklist.size() + "):").withStyle(ChatFormatting.AQUA));
        for (String id : cfg.junkBlacklist) {
            c.getSource().sendFeedback(Component.literal(" - " + id).withStyle(ChatFormatting.GRAY));
        }
        return 1;
    }

    public static String canonicalize(String raw) {
        String s = raw.trim().toLowerCase();
        return s.contains(":") ? s : "minecraft:" + s;
    }

    public static String validateAndCanonicalize(String raw) {
        String id = canonicalize(raw);
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
        if (item == null || BuiltInRegistries.ITEM.getKey(item).toString().equals("minecraft:air")) {
            return null;
        }
        return id;
    }
}
