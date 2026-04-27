package com.intrinsic.client.feature;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import com.intrinsic.client.mixin.SimpleOptionAccessor;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class FovUnlockFeature {
    public static final int MIN_FOV = 30;
    public static final int MAX_FOV = 170;

    private static int lastApplied = Integer.MIN_VALUE;

    private FovUnlockFeature() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("ifov")
                    .then(ClientCommands.argument("value", IntegerArgumentType.integer(MIN_FOV, MAX_FOV))
                            .executes(c -> {
                                int v = IntegerArgumentType.getInteger(c, "value");
                                IntrinsicConfig cfg = IntrinsicClient.getConfig();
                                cfg.fovOverride = v;
                                cfg.fovOverrideEnabled = true;
                                Feature.FOV_UNLOCK.setEnabled(true);
                                cfg.save();
                                applyNow(Minecraft.getInstance(), v);
                                c.getSource().sendFeedback(Component.literal("FOV set to " + v)
                                        .withStyle(ChatFormatting.AQUA));
                                return 1;
                            })));
        });
    }

    public static void tick(Minecraft client) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        if (!Feature.FOV_UNLOCK.isEnabled() || !cfg.fovOverrideEnabled) {
            lastApplied = Integer.MIN_VALUE;
            return;
        }
        int desired = Mth.clamp(cfg.fovOverride, MIN_FOV, MAX_FOV);
        OptionInstance<Integer> fov = client.options.fov();
        if (!fov.get().equals(desired) || lastApplied != desired) {
            applyNow(client, desired);
            lastApplied = desired;
        }
    }

    @SuppressWarnings("unchecked")
    private static void applyNow(Minecraft client, int value) {
        // Bypass the OptionInstance value clamp (vanilla caps fov at 110) by
        // writing the backing field directly via the accessor mixin.
        OptionInstance<Integer> fov = client.options.fov();
        ((SimpleOptionAccessor) (Object) fov).intrinsic_setValue(value);
    }
}
