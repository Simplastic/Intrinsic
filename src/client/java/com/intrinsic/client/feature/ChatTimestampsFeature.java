package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatTimestampsFeature {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    public static boolean isEnabled() {
        return IntrinsicClient.getConfig().chatTimestamps;
    }

    public static Component prefix(Component original) {
        if (alreadyStamped(original)) return original;
        String stamp = "[" + LocalTime.now().format(FMT) + "] ";
        // Component.empty() is styleless: the container does not cascade DARK_GRAY onto the
        // original message, so the timestamp is the only grey part.
        return Component.empty()
                .append(Component.literal(stamp).withStyle(ChatFormatting.DARK_GRAY))
                .append(original);
    }

    private static boolean alreadyStamped(Component t) {
        String s = t.getString();
        return s.length() >= 8
                && s.charAt(0) == '['
                && Character.isDigit(s.charAt(1))
                && Character.isDigit(s.charAt(2))
                && s.charAt(3) == ':'
                && Character.isDigit(s.charAt(4))
                && Character.isDigit(s.charAt(5))
                && s.charAt(6) == ']';
    }
}
