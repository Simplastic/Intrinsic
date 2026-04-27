package com.intrinsic;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntrinsicMod implements ModInitializer {
    public static final String MOD_ID = "intrinsic";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Intrinsic loaded (common)");
    }
}
