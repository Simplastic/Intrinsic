package com.intrinsic.client.feature;

import net.minecraft.client.Minecraft;

/**
 * Dispatcher for {@link Feature.Kind#TRIGGER} features. Invoked from
 * {@link FeatureHotkeyHandler} on the rising edge of the bound key.
 *
 * <p>Keeps the trigger body next to the feature that owns it — each case
 * delegates to a public static method on the feature class so the behaviour
 * can also be fired from the GUI or another mixin if needed.
 */
public final class FeatureTriggers {
    private FeatureTriggers() {}

    public static void fire(Feature feature, Minecraft client) {
        switch (feature) {
            case JUNK_DROP -> JunkDropFeature.dropAll(client);
            case CLEAN_SCREENSHOT -> CleanScreenshotFeature.takeNow(client);
            case VIEW_LOCK -> ViewLockFeature.toggle(client);
            // INVENTORY_SORT and QUICK_STASH trigger inside container screens via
            // HandledScreenKeyMixin — they do not route through the out-of-screen
            // hotkey handler and therefore do not appear here.
            default -> {
                // No-op: feature marked TRIGGER but without a dispatch entry —
                // silently ignore rather than crash.
            }
        }
    }
}
