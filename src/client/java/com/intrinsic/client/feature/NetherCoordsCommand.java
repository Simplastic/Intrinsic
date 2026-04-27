package com.intrinsic.client.feature;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/**
 * <code>/inethercoords</code> — overworld ↔ nether coordinate converter.
 *
 * <ul>
 *   <li>No args: prints the player's current coords and the equivalent coords
 *       in the paired dimension (overworld↔nether). In the End or a custom
 *       dimension, prints the position and a note that conversion does not
 *       apply.</li>
 *   <li><code>/inethercoords &lt;x&gt; &lt;y&gt; &lt;z&gt;</code>: treats the
 *       input as <em>overworld</em> coords and prints the nether equivalent
 *       (<code>x/8, y, z/8</code>).</li>
 *   <li><code>/inethercoords from_nether &lt;x&gt; &lt;y&gt; &lt;z&gt;</code>:
 *       treats the input as <em>nether</em> coords and prints the overworld
 *       equivalent (<code>x*8, y, z*8</code>).</li>
 * </ul>
 */
public final class NetherCoordsCommand {
    private NetherCoordsCommand() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("inethercoords")
                    .executes(NetherCoordsCommand::runPlayer)
                    .then(ClientCommands.literal("from_nether")
                            .then(ClientCommands.argument("x", DoubleArgumentType.doubleArg())
                                    .then(ClientCommands.argument("y", DoubleArgumentType.doubleArg())
                                            .then(ClientCommands.argument("z", DoubleArgumentType.doubleArg())
                                                    .executes(c -> runExplicit(c, true))))))
                    .then(ClientCommands.argument("x", DoubleArgumentType.doubleArg())
                            .then(ClientCommands.argument("y", DoubleArgumentType.doubleArg())
                                    .then(ClientCommands.argument("z", DoubleArgumentType.doubleArg())
                                            .executes(c -> runExplicit(c, false))))));
        });
    }

    private static int runPlayer(CommandContext<FabricClientCommandSource> c) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            c.getSource().sendFeedback(Component.literal("No player loaded.").withStyle(ChatFormatting.RED));
            return 0;
        }
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        String dim = player.level().dimension().identifier().toString();

        if (player.level().dimension() == Level.OVERWORLD) {
            emitConversion(c, "Overworld", x, y, z, "Nether", x / 8.0, y, z / 8.0);
        } else if (player.level().dimension() == Level.NETHER) {
            emitConversion(c, "Nether", x, y, z, "Overworld", x * 8.0, y, z * 8.0);
        } else {
            c.getSource().sendFeedback(
                    Component.literal("You (" + fmt(x) + ", " + fmt(y) + ", " + fmt(z)
                            + ") \u2014 in " + dim + ". Overworld\u2194Nether conversion only applies to those two dimensions.")
                            .withStyle(ChatFormatting.GRAY));
        }
        return 1;
    }

    private static int runExplicit(CommandContext<FabricClientCommandSource> c, boolean fromNether) {
        double x = DoubleArgumentType.getDouble(c, "x");
        double y = DoubleArgumentType.getDouble(c, "y");
        double z = DoubleArgumentType.getDouble(c, "z");
        if (fromNether) {
            emitConversion(c, "Nether", x, y, z, "Overworld", x * 8.0, y, z * 8.0);
        } else {
            emitConversion(c, "Overworld", x, y, z, "Nether", x / 8.0, y, z / 8.0);
        }
        return 1;
    }

    private static void emitConversion(CommandContext<FabricClientCommandSource> c,
                                        String fromLabel, double fx, double fy, double fz,
                                        String toLabel, double tx, double ty, double tz) {
        c.getSource().sendFeedback(
                Component.literal(fromLabel + " (" + fmt(fx) + ", " + fmt(fy) + ", " + fmt(fz) + ")")
                        .withStyle(ChatFormatting.AQUA));
        c.getSource().sendFeedback(
                Component.literal(" \u2192 " + toLabel + " (" + fmt(tx) + ", " + fmt(ty) + ", " + fmt(tz) + ")")
                        .withStyle(ChatFormatting.YELLOW));
    }

    private static String fmt(double v) {
        // Portal linking rounds toward the floor on x/z; print whole numbers to match that intuition.
        return Long.toString((long) Math.floor(v));
    }
}
