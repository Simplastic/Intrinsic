package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.util.ShulkerUtil;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

public final class ShulkerTooltipSuppressor {
    private ShulkerTooltipSuppressor() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (!IntrinsicClient.getConfig().shulkerPreview) return;
            if (!ShulkerUtil.isShulkerBox(stack)) return;
            if (lines.size() <= 1) return;
            lines.subList(1, lines.size()).clear();
        });
    }
}
