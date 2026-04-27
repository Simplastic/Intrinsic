package com.intrinsic.client.util;

import java.util.Random;

public class SlimeChunkUtil {
    public static boolean isSlimeChunk(long worldSeed, int chunkX, int chunkZ) {
        long seed = worldSeed
                + (long) (chunkX * chunkX * 0x4c1906)
                + (long) (chunkX * 0x5ac0db)
                + (long) (chunkZ * chunkZ) * 0x4307a7L
                + (long) (chunkZ * 0x5f24f)
                ^ 0x3ad8025fL;
        Random rand = new Random(seed);
        return rand.nextInt(10) == 0;
    }
}
