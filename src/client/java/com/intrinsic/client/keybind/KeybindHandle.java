package com.intrinsic.client.keybind;

public interface KeybindHandle {
    String label();

    int keyCode();

    void setKeyCode(int code);

    default void clear() { setKeyCode(-1); }

    String displayName();
}
