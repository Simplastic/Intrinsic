package com.intrinsic.client.keybind;

import com.intrinsic.client.gui.IntrinsicScreen;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

// Only the "Open Intrinsic Menu" keybind is registered as a vanilla KeyMapping
// so it shows up in Options > Controls. Every other mod keybind is routed
// through DynamicKeyHandle (config-stored) and polled by FeatureHotkeyHandler
// or HandledScreenKeyMixin against the raw keycode.
public class ModKeybinds {
    public static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("intrinsic", "main"));

    public static KeyMapping openMenu;

    public static void register() {
        openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.intrinsic.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                CATEGORY
        ));
    }

    public static void handleTick(Minecraft client) {
        while (openMenu.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(new IntrinsicScreen(null));
            }
        }
    }
}
