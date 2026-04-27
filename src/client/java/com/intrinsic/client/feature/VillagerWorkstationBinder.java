package com.intrinsic.client.feature;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class VillagerWorkstationBinder {
    private static final int SEARCH_RADIUS = 16;
    private static final double PROFESSION_WATCH_RADIUS_SQ = 48.0 * 48.0;

    private static final int MISSING_VILLAGER_RELEASE_TICKS = 60;

    private static final Map<UUID, BlockPos> villagerToWorkstation = new HashMap<>();
    private static final Map<BlockPos, UUID> workstationToVillager = new HashMap<>();
    private static final Map<UUID, String> knownProfession = new HashMap<>();
    private static final Map<UUID, String> nearbyProfession = new HashMap<>();
    private static final Map<UUID, Integer> missCounter = new HashMap<>();
    private static int particleTickCounter = 0;

    private VillagerWorkstationBinder() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(VillagerWorkstationBinder::tick);
    }

    public static BlockPos workstationOf(UUID villagerId) {
        return villagerToWorkstation.get(villagerId);
    }

    public static UUID villagerAt(BlockPos workstation) {
        return workstationToVillager.get(workstation.immutable());
    }

    public static boolean isWorkstation(Block block) {
        return WorkstationProfessions.professionOf(block) != null;
    }

    public static void bindFromProbe(Villager villager) {
        if (villager == null) return;
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) return;

        String expectedProfession = WorkstationProfessions.professionIdOf(villager);
        if (expectedProfession == null) return;

        BlockPos center = villager.blockPosition();
        BlockPos best = null;
        double bestSq = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dy = -6; dy <= 6; dy++) {
                for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    String prof = WorkstationProfessions.professionOf(state.getBlock());
                    if (prof == null || !prof.equals(expectedProfession)) continue;
                    BlockPos ws = cursor.immutable();
                    if (workstationToVillager.containsKey(ws)
                            && !villager.getUUID().equals(workstationToVillager.get(ws))) continue;
                    double distSq = ws.distSqr(center);
                    if (distSq < bestSq) {
                        bestSq = distSq;
                        best = ws;
                    }
                }
            }
        }

        if (best != null) {
            bindPair(villager.getUUID(), best);
            knownProfession.put(villager.getUUID(), expectedProfession);
        }
    }

    private static void bindPair(UUID id, BlockPos pos) {
        BlockPos old = villagerToWorkstation.put(id, pos);
        if (old != null && !old.equals(pos)) workstationToVillager.remove(old);
        UUID oldOwner = workstationToVillager.put(pos, id);
        if (oldOwner != null && !oldOwner.equals(id)) {
            villagerToWorkstation.remove(oldOwner);
            VillagerTradeCache.invalidate(oldOwner);
        }
    }

    public static void invalidate(UUID villagerId) {
        BlockPos pos = villagerToWorkstation.remove(villagerId);
        if (pos != null) workstationToVillager.remove(pos);
        knownProfession.remove(villagerId);
        VillagerTradeCache.invalidate(villagerId);
    }

    public static void onBlockChanged(BlockPos pos, BlockState oldState, BlockState newState) {
        BlockPos key = pos.immutable();
        boolean wasWorkstation = oldState != null && WorkstationProfessions.professionOf(oldState.getBlock()) != null;
        boolean isWorkstation = newState != null && WorkstationProfessions.professionOf(newState.getBlock()) != null;
        if (!wasWorkstation && !isWorkstation) return;

        UUID owner = workstationToVillager.remove(key);
        if (owner != null) {
            villagerToWorkstation.remove(owner);
            knownProfession.remove(owner);
            VillagerTradeCache.invalidate(owner);
            VillagerAutoProbe.clearCooldown(owner);
        }
    }

    private static void tick(Minecraft client) {
        ClientLevel level = client.level;
        if (level == null) {
            villagerToWorkstation.clear();
            workstationToVillager.clear();
            knownProfession.clear();
            nearbyProfession.clear();
            missCounter.clear();
            return;
        }

        if (client.player != null) {
            double px = client.player.getX(), py = client.player.getY(), pz = client.player.getZ();
            java.util.HashSet<UUID> seen = new java.util.HashSet<>();
            for (Entity e : level.entitiesForRendering()) {
                if (!(e instanceof Villager v)) continue;
                double dx = v.getX() - px, dy = v.getY() - py, dz = v.getZ() - pz;
                if (dx * dx + dy * dy + dz * dz > PROFESSION_WATCH_RADIUS_SQ) continue;
                UUID id = v.getUUID();
                seen.add(id);
                String current = WorkstationProfessions.professionIdOf(v);
                String prev = nearbyProfession.get(id);
                boolean changed = (prev == null) ? (current != null) : !prev.equals(current);
                if (changed) {
                    VillagerTradeCache.invalidate(id);
                    VillagerAutoProbe.clearCooldown(id);
                }
                if (current == null) nearbyProfession.remove(id);
                else nearbyProfession.put(id, current);
            }
            nearbyProfession.keySet().retainAll(seen);
        }

        particleTickCounter++;
        boolean particleTick = Feature.VILLAGER_TRADES.isEnabled()
                && (particleTickCounter % 5) == 0;

        BlockPos hitBlock = null;
        UUID hitVillager = null;
        if (particleTick && client.hitResult != null) {
            if (client.hitResult instanceof net.minecraft.world.phys.BlockHitResult bh) {
                hitBlock = bh.getBlockPos();
            } else if (client.hitResult instanceof net.minecraft.world.phys.EntityHitResult eh
                    && eh.getEntity() instanceof Villager hv) {
                hitVillager = hv.getUUID();
            }
        }

        var it = villagerToWorkstation.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            UUID id = entry.getKey();
            BlockPos ws = entry.getValue();
            Villager v = findVillager(level, id);

            boolean emit = particleTick
                    && ((hitBlock != null && hitBlock.equals(ws)) || id.equals(hitVillager));
            if (emit) {
                java.util.Random rng = java.util.concurrent.ThreadLocalRandom.current();
                double px = ws.getX() + 0.3 + rng.nextDouble() * 0.4;
                double py = ws.getY() + 1.05 + rng.nextDouble() * 0.1;
                double pz = ws.getZ() + 0.3 + rng.nextDouble() * 0.4;
                level.addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0.0, 0.02, 0.0);
            }

            if (v == null) {
                if (level.isLoaded(ws)) {
                    int miss = missCounter.getOrDefault(id, 0) + 1;
                    if (miss >= MISSING_VILLAGER_RELEASE_TICKS) {
                        it.remove();
                        workstationToVillager.remove(ws);
                        knownProfession.remove(id);
                        VillagerTradeCache.invalidate(id);
                        VillagerAutoProbe.clearCooldown(id);
                        missCounter.remove(id);
                    } else {
                        missCounter.put(id, miss);
                    }
                }
                continue;
            }
            missCounter.remove(id);

            String current = WorkstationProfessions.professionIdOf(v);
            String known = knownProfession.get(id);
            if (current == null || (known != null && !known.equals(current))) {
                it.remove();
                workstationToVillager.remove(ws);
                knownProfession.remove(id);
                VillagerTradeCache.invalidate(id);
                VillagerAutoProbe.clearCooldown(id);
                continue;
            }

            BlockState wsState = level.getBlockState(ws);
            String wsProf = WorkstationProfessions.professionOf(wsState.getBlock());
            if (wsProf == null || !wsProf.equals(current)) {
                it.remove();
                workstationToVillager.remove(ws);
                knownProfession.remove(id);
                VillagerTradeCache.invalidate(id);
                VillagerAutoProbe.clearCooldown(id);
            }
        }
    }

    private static Villager findVillager(ClientLevel level, UUID id) {
        for (Entity e : level.entitiesForRendering()) {
            if (e instanceof Villager v && v.getUUID().equals(id)) return v;
        }
        return null;
    }

    public static final class WorkstationProfessions {
        private static final Map<Block, String> MAP = new HashMap<>();
        static {
            MAP.put(Blocks.COMPOSTER,         "minecraft:farmer");
            MAP.put(Blocks.SMOKER,            "minecraft:butcher");
            MAP.put(Blocks.BLAST_FURNACE,     "minecraft:armorer");
            MAP.put(Blocks.CARTOGRAPHY_TABLE, "minecraft:cartographer");
            MAP.put(Blocks.BREWING_STAND,     "minecraft:cleric");
            MAP.put(Blocks.CAULDRON,          "minecraft:leatherworker");
            MAP.put(Blocks.FLETCHING_TABLE,   "minecraft:fletcher");
            MAP.put(Blocks.GRINDSTONE,        "minecraft:weaponsmith");
            MAP.put(Blocks.LECTERN,           "minecraft:librarian");
            MAP.put(Blocks.LOOM,              "minecraft:shepherd");
            MAP.put(Blocks.SMITHING_TABLE,    "minecraft:toolsmith");
            MAP.put(Blocks.STONECUTTER,       "minecraft:mason");
            MAP.put(Blocks.BARREL,            "minecraft:fisherman");
        }

        private WorkstationProfessions() {}

        public static String professionOf(Block block) {
            return MAP.get(block);
        }

        public static String professionIdOf(Villager v) {
            if (v == null) return null;
            var prof = v.getVillagerData().profession();
            var key = prof.unwrapKey().orElse(null);
            if (key == null) return null;
            String id = key.identifier().toString();
            if ("minecraft:none".equals(id)) return null;
            return id;
        }
    }
}
