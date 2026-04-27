package com.intrinsic.client.keybind;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.feature.Feature;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;

public final class DynamicKeyHandle implements KeybindHandle {
    private final Feature feature;

    public DynamicKeyHandle(Feature feature) {
        this.feature = feature;
    }

    @Override
    public String label() {
        int k = feature.getKeyCode();
        if (k < 0) return "—";
        try {
            return InputConstants.getKey(new KeyEvent(k, 0, 0)).getDisplayName().getString();
        } catch (Exception e) {
            return "?";
        }
    }

    @Override
    public int keyCode() {
        return feature.getKeyCode();
    }

    @Override
    public void setKeyCode(int code) {
        feature.setKeyCode(code);
        IntrinsicClient.getConfig().save();
    }

    @Override
    public String displayName() {
        return feature.displayName;
    }
}
