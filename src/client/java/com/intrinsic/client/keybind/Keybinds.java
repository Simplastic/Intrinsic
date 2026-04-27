package com.intrinsic.client.keybind;

import com.intrinsic.client.feature.Feature;

public final class Keybinds {
    private Keybinds() {}

    public static KeybindHandle handleFor(Feature feature) {
        return new DynamicKeyHandle(feature);
    }

    public static KeybindHandle openMenuHandle() {
        return new VanillaKeyBindingHandle(ModKeybinds.openMenu, "Open Intrinsic Menu");
    }
}
