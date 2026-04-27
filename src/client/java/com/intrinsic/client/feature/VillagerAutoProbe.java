package com.intrinsic.client.feature;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class VillagerAutoProbe {
    private static final long PER_VILLAGER_COOLDOWN_MS = 5000L;
    private static final long GLOBAL_COOLDOWN_MS = 800L;
    private static final long PROBE_TIMEOUT_MS = 2000L;
    private static final long SILENCE_WINDOW_MS = 800L;
    private static final double SILENCE_RADIUS_SQ = 4.0 * 4.0;

    private static final Map<UUID, Long> lastProbedAt = new HashMap<>();
    private static long lastGlobalProbeMs = 0L;

    private static UUID probingUuid = null;
    private static long probeStartedMs = 0L;
    private static long lastSilenceAnchorMs = 0L;
    private static Vec3 probedVillagerPos = null;

    private VillagerAutoProbe() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(VillagerAutoProbe::tick);
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (hand == InteractionHand.MAIN_HAND) {
                if (entity instanceof Villager v) {
                    VillagerInteractionTracker.record(v);
                } else if (entity instanceof AbstractVillager) {
                    VillagerInteractionTracker.clear();
                }
            }
            return InteractionResult.PASS;
        });
    }

    public static boolean isProbing() {
        return probingUuid != null;
    }

    public static Vec3 probedVillagerPos() {
        return probedVillagerPos;
    }

    public static void clearCooldown(UUID id) {
        if (id != null) lastProbedAt.remove(id);
    }

    public static boolean mutesSound(SoundInstance instance) {
        if (instance == null) return false;
        long now = System.currentTimeMillis();
        if (now - lastSilenceAnchorMs > SILENCE_WINDOW_MS) return false;
        if (probedVillagerPos == null) return false;
        var id = instance.getIdentifier();
        if (id == null) return false;
        String path = id.getPath();
        if (!(path.startsWith("ui.") || path.startsWith("entity.villager.") || path.startsWith("block."))) {
            return false;
        }
        double dx = instance.getX() - probedVillagerPos.x;
        double dy = instance.getY() - probedVillagerPos.y;
        double dz = instance.getZ() - probedVillagerPos.z;
        return (dx * dx + dy * dy + dz * dz) <= SILENCE_RADIUS_SQ;
    }

    public static boolean isEligible(Villager villager) {
        if (villager == null || !villager.isAlive()) return false;
        if (villager.isBaby()) return false;
        return VillagerWorkstationBinder.WorkstationProfessions.professionIdOf(villager) != null;
    }

    public static boolean maybeProbe(Villager villager) {
        if (villager == null) return false;
        if (!Feature.VILLAGER_AUTO_PROBE.isEnabled()) return false;
        if (!isEligible(villager)) return false;

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.level == null) return false;
        if (client.screen != null) return false;
        if (probingUuid != null) return false;
        if (player.connection == null) return false;

        long now = System.currentTimeMillis();
        if (now - lastGlobalProbeMs < GLOBAL_COOLDOWN_MS) return false;

        UUID id = villager.getUUID();
        Long last = lastProbedAt.get(id);
        if (last != null && now - last < PER_VILLAGER_COOLDOWN_MS) return false;

        probingUuid = id;
        probeStartedMs = now;
        lastGlobalProbeMs = now;
        lastSilenceAnchorMs = now;
        probedVillagerPos = villager.position();
        lastProbedAt.put(id, now);

        VillagerInteractionTracker.record(villager);

        Vec3 hitOffset = villager.position().add(0, villager.getBbHeight() * 0.5, 0).subtract(villager.position());
        player.connection.send(new ServerboundInteractPacket(
                villager.getId(), InteractionHand.MAIN_HAND, hitOffset, player.isShiftKeyDown()));
        return true;
    }

    private static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            if (probingUuid != null) reset();
            return;
        }

        if (probingUuid == null) {
            if (client.screen instanceof MerchantScreen
                    && player.containerMenu instanceof MerchantMenu merchant) {
                MerchantOffers offers = merchant.getOffers();
                if (offers != null && !offers.isEmpty() && VillagerInteractionTracker.isFresh(1500L)) {
                    Villager v = VillagerInteractionTracker.lastInteracted();
                    if (v != null && v.isAlive() && isEligible(v)) {
                        VillagerTradeCache.CachedTrades existing = VillagerTradeCache.get(v.getUUID());
                        if (existing == null || existing.offers.size() != offers.size()
                                || existing.level != v.getVillagerData().level()) {
                            VillagerTradeCache.record(v, offers);
                            VillagerWorkstationBinder.bindFromProbe(v);
                        }
                    }
                }
            }
            return;
        }

        long now = System.currentTimeMillis();
        if (now - probeStartedMs > PROBE_TIMEOUT_MS) {
            reset();
            return;
        }

        if (player.containerMenu instanceof MerchantMenu merchant) {
            MerchantOffers offers = merchant.getOffers();
            if (offers != null && !offers.isEmpty()) {
                Villager villager = findVillager(client.level, probingUuid);
                if (villager != null) {
                    VillagerTradeCache.record(villager, offers);
                    VillagerWorkstationBinder.bindFromProbe(villager);
                }
                if (player.connection != null) {
                    player.connection.send(new ServerboundContainerClosePacket(merchant.containerId));
                }
                player.clientSideCloseContainer();
                lastSilenceAnchorMs = now;
                reset();
            }
        }
    }

    private static Villager findVillager(ClientLevel level, UUID id) {
        for (Entity e : level.entitiesForRendering()) {
            if (e instanceof Villager v && v.getUUID().equals(id)) return v;
        }
        return null;
    }

    private static void reset() {
        probingUuid = null;
        probeStartedMs = 0L;
        probedVillagerPos = null;
    }
}
