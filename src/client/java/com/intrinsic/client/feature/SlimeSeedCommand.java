package com.intrinsic.client.feature;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.intrinsic.client.IntrinsicClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;

public class SlimeSeedCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("iseed")
                    .then(ClientCommands.argument("seed", LongArgumentType.longArg())
                            .executes(ctx -> {
                                long seed = LongArgumentType.getLong(ctx, "seed");
                                IntrinsicClient.getConfig().worldSeed = seed;
                                IntrinsicClient.getConfig().save();
                                ctx.getSource().sendFeedback(
                                        Component.literal("Intrinsic: slime chunk seed set to " + seed));
                                return 1;
                            }))
                    .executes(ctx -> {
                        long seed = IntrinsicClient.getConfig().worldSeed;
                        ctx.getSource().sendFeedback(
                                Component.literal("Intrinsic: current seed = " + seed
                                        + " (use /iseed <seed> to change)"));
                        return 1;
                    }));
        });
    }
}
