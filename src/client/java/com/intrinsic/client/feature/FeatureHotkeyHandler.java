package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.hud.ToggleToastHud;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

import java.util.HashSet;
import java.util.Set;

public class FeatureHotkeyHandler {
    private static final Set<Integer> wasDown = new HashSet<>();

    public static void tick(Minecraft client) {
        if (client.screen != null) {
            wasDown.clear();
            return;
        }
        Window window = client.getWindow();
        Set<Integer> nowDown = new HashSet<>();
        for (Feature f : Feature.values()) {
            int key = f.getKeyCode();
            if (key < 0) continue;
            boolean down = InputConstants.isKeyDown(window, key);
            if (down) nowDown.add(key);
            if (down && !wasDown.contains(key)) {
                switch (f.kind) {
                    case TOGGLE -> {
                        f.toggle();
                        ToggleToastHud.push(f);
                        IntrinsicClient.getConfig().save();
                    }
                    case TRIGGER -> FeatureTriggers.fire(f, client);
                    case HOLD -> {
                        // Hold features poll their own key inside tick() (e.g. ZoomFeature);
                        // no edge action here.
                    }
                }
            }
        }
        wasDown.clear();
        wasDown.addAll(nowDown);
    }
}
