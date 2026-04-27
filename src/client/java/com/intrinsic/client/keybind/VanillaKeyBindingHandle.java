package com.intrinsic.client.keybind;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

public final class VanillaKeyBindingHandle implements KeybindHandle {
    private final KeyMapping binding;
    private final String displayName;

    public VanillaKeyBindingHandle(KeyMapping binding, String displayName) {
        this.binding = binding;
        this.displayName = displayName;
    }

    @Override
    public String label() {
        if (binding.isUnbound()) return "—";
        return binding.getTranslatedKeyMessage().getString();
    }

    @Override
    public int keyCode() {
        InputConstants.Key key = currentBoundKey();
        if (key.getType() != InputConstants.Type.KEYSYM) return -1;
        int code = key.getValue();
        return code == InputConstants.UNKNOWN.getValue() ? -1 : code;
    }

    @Override
    public void setKeyCode(int code) {
        InputConstants.Key key = (code < 0)
                ? InputConstants.UNKNOWN
                : InputConstants.Type.KEYSYM.getOrCreate(code);
        binding.setKey(key);
        KeyMapping.resetMapping();
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null) mc.options.save();
    }

    @Override
    public String displayName() {
        return displayName;
    }

    private InputConstants.Key currentBoundKey() {
        // KeyMapping exposes no getBoundKey() in 1.21.11, but we can round-trip through
        // the translation key, which is always in sync with the current binding.
        return InputConstants.getKey(binding.saveString());
    }
}
