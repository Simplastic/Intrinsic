package com.intrinsic.client.audio;

import java.util.List;

public final class AudioControl {
    public final String id;
    public final String displayName;
    public final String description;
    public final List<String> prefixes;

    public AudioControl(String id, String displayName, String description, List<String> prefixes) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.prefixes = prefixes;
    }
}
