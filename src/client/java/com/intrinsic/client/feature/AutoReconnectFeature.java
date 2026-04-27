package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.TitleScreen;

public class AutoReconnectFeature {
    private static ServerData lastServer;

    public static boolean isEnabled() {
        return IntrinsicClient.getConfig().autoReconnect;
    }

    public static void recordServer(ServerData info) {
        if (info != null) lastServer = info;
    }

    public static ServerData getLastServer() {
        return lastServer;
    }

    public static void reconnect(Minecraft client, Screen parent) {
        if (lastServer == null) return;
        Screen back = parent != null ? parent : new TitleScreen();
        ConnectScreen.startConnecting(new JoinMultiplayerScreen(back), client,
                ServerAddress.parseString(lastServer.ip), lastServer, false, null);
    }
}
