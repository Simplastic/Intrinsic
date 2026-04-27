package com.intrinsic.client.audio;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AudioRegistry {
    private static final Map<String, AudioControl> CONTROLS = new LinkedHashMap<>();

    static {
        add("redstone_pistons",         "Pistons",              "Piston extend and contract.",
                List.of("block.piston."));
        add("redstone_comparators",     "Comparators",          "Redstone comparator click.",
                List.of("block.comparator."));
        add("redstone_dispensers",      "Dispensers & Droppers","Dispense, drop and launch sounds.",
                List.of("block.dispenser."));
        add("redstone_buttons_levers",  "Buttons & Levers",     "Buttons, levers, pressure plates and tripwire clicks.",
                List.of("block.stone_button.", "block.wood_button.", "block.lever.",
                        "block.stone_pressure_plate.", "block.wooden_pressure_plate.",
                        "block.metal_pressure_plate.", "block.tripwire."));
        add("redstone_note_blocks",     "Note Blocks",          "All note block pitches and instruments.",
                List.of("block.note_block."));
        add("redstone_doors",           "Doors & Trapdoors",    "Iron + wooden doors and trapdoors (mechanism side).",
                List.of("block.iron_door.", "block.iron_trapdoor.",
                        "block.wooden_door.", "block.wooden_trapdoor."));
        add("weather",                  "Weather",              "Rain, thunder, lightning impact.",
                List.of("weather.rain", "entity.lightning_bolt."));
        add("water_drip",               "Water / Lava Drip",    "Pointed dripstone, cave drip, bubble column.",
                List.of("block.pointed_dripstone.", "block.bubble_column.",
                        "block.water.", "block.lava."));
        add("villager",                 "Villagers",            "Villager voices and trade sounds.",
                List.of("entity.villager.", "entity.wandering_trader."));
        add("ambient_mobs",             "Ambient Mobs",         "Idle ambient mob sounds (passive + hostile).",
                List.of("entity.bat.", "entity.cat.ambient", "entity.cow.ambient",
                        "entity.chicken.ambient", "entity.sheep.ambient", "entity.pig.ambient",
                        "entity.zombie.ambient", "entity.skeleton.ambient", "entity.creeper.primed",
                        "entity.spider.ambient", "entity.enderman.ambient"));
    }

    private static void add(String id, String name, String desc, List<String> prefixes) {
        CONTROLS.put(id, new AudioControl(id, name, desc, prefixes));
    }

    public static Map<String, AudioControl> builtins() {
        return CONTROLS;
    }

    public static AudioControl get(String id) {
        return CONTROLS.get(id);
    }

    public static double getVolume(String id) {
        Double v = IntrinsicClient.getConfig().soundVolumes.get(id);
        return v == null ? 1.0 : v;
    }

    public static void setVolume(String id, double v) {
        double clamped = Math.max(0.0, Math.min(2.0, v));
        IntrinsicClient.getConfig().soundVolumes.put(id, clamped);
    }

    public static void resetVolume(String id) {
        IntrinsicClient.getConfig().soundVolumes.remove(id);
    }

    public static List<IntrinsicConfig.CustomAudioPattern> customPatterns() {
        return IntrinsicClient.getConfig().customAudioPatterns;
    }

    public static void addCustomPattern(String displayName, String pattern) {
        addCustomPattern(displayName, pattern, 1.0);
    }

    public static void addCustomPattern(String displayName, String pattern, double volume) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        String id = "custom_" + System.currentTimeMillis();
        cfg.customAudioPatterns.add(new IntrinsicConfig.CustomAudioPattern(id, displayName, pattern, volume));
    }

    public static void updateCustomPattern(String id, String displayName, String pattern, double volume) {
        for (IntrinsicConfig.CustomAudioPattern p : IntrinsicClient.getConfig().customAudioPatterns) {
            if (id.equals(p.id)) {
                p.displayName = displayName;
                p.pattern = pattern;
                p.volume = volume;
                return;
            }
        }
    }

    public static void removeCustomPattern(String id) {
        IntrinsicClient.getConfig().customAudioPatterns.removeIf(p -> id.equals(p.id));
    }

    public static float getMultiplier(SoundInstance instance) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        if (!cfg.audioTuning) return 1.0f;
        Identifier id = instance.getIdentifier();
        if (id == null) return 1.0f;
        String soundId = id.getPath();

        double mult = 1.0;
        boolean matched = false;

        for (AudioControl ctrl : CONTROLS.values()) {
            for (String prefix : ctrl.prefixes) {
                if (soundId.startsWith(prefix) || soundId.equals(stripTrailingDot(prefix))) {
                    double v = getVolume(ctrl.id);
                    if (!matched || v < mult) mult = v;
                    matched = true;
                    break;
                }
            }
        }

        for (IntrinsicConfig.CustomAudioPattern pat : cfg.customAudioPatterns) {
            if (pat.pattern != null && !pat.pattern.isEmpty() && soundId.startsWith(pat.pattern)) {
                if (!matched || pat.volume < mult) mult = pat.volume;
                matched = true;
            }
        }

        return matched ? (float) mult : 1.0f;
    }

    private static String stripTrailingDot(String s) {
        return s.endsWith(".") ? s.substring(0, s.length() - 1) : s;
    }

    private AudioRegistry() {}
}
