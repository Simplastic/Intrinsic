package com.intrinsic.client.feature;

import net.minecraft.client.Minecraft;

public final class HitboxFeature {
    private HitboxFeature() {}

    // The mod's toggle no longer mutates DebugScreenEntries — F3+B and the mod
    // toggle are independent. DebugScreenEntryListMixin ORs our flag into
    // isCurrentlyEnabled(ENTITY_HITBOXES) so the rest of the renderer sees the
    // union without us touching vanilla state.
    public static void tick(Minecraft client) {
        // intentionally empty — kept for call-site stability
    }
}
