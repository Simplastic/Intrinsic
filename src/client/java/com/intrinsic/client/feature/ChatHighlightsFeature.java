package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import com.intrinsic.client.config.IntrinsicConfig.ChatHighlightRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class ChatHighlightsFeature {
    // Throttle sound to one play per chat message regardless of how many
    // rules or matches fire.
    private static int compiledFromHash = 0;
    private static List<Compiled> compiled = new ArrayList<>();
    private static boolean defaultInstalled = false;

    private ChatHighlightsFeature() {}

    public static boolean isEnabled() {
        return IntrinsicClient.getConfig().chatHighlightsEnabled
                && Feature.CHAT_HIGHLIGHTS.isEnabled();
    }

    public static Component apply(Component message) {
        if (!isEnabled()) return message;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return message;

        ensureDefaultRule(client);
        recompileIfDirty();
        if (compiled.isEmpty()) return message;

        String plain = message.getString();
        if (plain.isEmpty()) return message;

        // Find the earliest match across all rules (longest wins on tie).
        Compiled firstRule = null;
        Matcher firstMatch = null;
        int firstStart = Integer.MAX_VALUE;
        for (Compiled c : compiled) {
            Matcher m = c.pattern.matcher(plain);
            if (!m.find()) continue;
            if (m.start() < firstStart || (m.start() == firstStart
                    && firstMatch != null
                    && (m.end() - m.start()) > (firstMatch.end() - firstMatch.start()))) {
                firstStart = m.start();
                firstMatch = m;
                firstRule = c;
            }
        }
        if (firstRule == null) return message;

        playSound(client, firstRule.soundId);

        // Rebuild with the matched span recolored. We keep the original message
        // as the tail (with matches-after-this ignored) to preserve vanilla
        // formatting outside the highlight. Multiple matches: re-entrant apply.
        MutableComponent out = Component.empty();
        if (firstMatch.start() > 0) {
            out.append(Component.literal(plain.substring(0, firstMatch.start())));
        }
        out.append(Component.literal(plain.substring(firstMatch.start(), firstMatch.end()))
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(firstRule.argb & 0xFFFFFF)).withBold(true)));
        String tail = plain.substring(firstMatch.end());
        if (!tail.isEmpty()) {
            out.append(apply(Component.literal(tail)));
        }
        return out;
    }

    private static void playSound(Minecraft client, String soundId) {
        if (soundId == null || soundId.isBlank()) return;
        Identifier id = Identifier.tryParse(soundId);
        if (id == null) return;
        SoundEvent ev = SoundEvent.createVariableRangeEvent(id);
        client.getSoundManager().play(SimpleSoundInstance.forUI(ev, 1.0f, 0.8f));
    }

    private static void ensureDefaultRule(Minecraft client) {
        if (defaultInstalled) return;
        if (client.getUser() == null) return;
        String name = client.getUser().getName();
        if (name == null || name.isBlank()) return;
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        boolean haveNameRule = cfg.chatHighlights.stream()
                .anyMatch(r -> r.pattern != null && r.pattern.toLowerCase().contains(name.toLowerCase()));
        if (!haveNameRule) {
            cfg.chatHighlights.add(new ChatHighlightRule(
                    "(?i)\\b" + Pattern.quote(name) + "\\b",
                    0xFFFFD54A,
                    "entity.experience_orb.pickup"));
            cfg.save();
        }
        defaultInstalled = true;
    }

    private static void recompileIfDirty() {
        List<ChatHighlightRule> rules = IntrinsicClient.getConfig().chatHighlights;
        int h = signature(rules);
        if (h == compiledFromHash) return;
        List<Compiled> fresh = new ArrayList<>(rules.size());
        for (ChatHighlightRule r : rules) {
            if (r == null || r.pattern == null || r.pattern.isBlank()) continue;
            try {
                fresh.add(new Compiled(Pattern.compile(r.pattern), r.argb, r.soundId));
            } catch (PatternSyntaxException ignored) {
                // Invalid user regex — skip this rule silently.
            }
        }
        compiled = fresh;
        compiledFromHash = h;
    }

    private static int signature(List<ChatHighlightRule> rules) {
        int h = 1;
        for (ChatHighlightRule r : rules) {
            if (r == null) { h = h * 31; continue; }
            h = h * 31 + (r.pattern == null ? 0 : r.pattern.hashCode());
            h = h * 31 + r.argb;
            h = h * 31 + (r.soundId == null ? 0 : r.soundId.hashCode());
        }
        return h;
    }

    private record Compiled(Pattern pattern, int argb, String soundId) {}
}
