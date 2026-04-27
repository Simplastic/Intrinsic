package com.intrinsic.client.gui.widget;

public final class PanelChrome {
    public static final int PANEL_PAD = 20;
    public static final int PANEL_MAX_W = 820;
    public static final int PANEL_MAX_H = 540;
    public static final int SIDEBAR_W = 156;
    public static final int HEADER_H = 36;
    public static final int FOOTER_H = 36;
    public static final int CARD_H = 74;
    public static final int CARD_GAP = 8;
    public static final int PILL_W = 54;
    public static final int PILL_H = 20;
    public static final int CHIP_H = 16;
    public static final int SIDEBAR_ROW_H = 22;

    private PanelChrome() {}

    public static int panelW(int screenW) {
        return Math.min(screenW - PANEL_PAD * 2, PANEL_MAX_W);
    }

    public static int panelH(int screenH) {
        return Math.min(screenH - PANEL_PAD, PANEL_MAX_H);
    }

    public static int panelX(int screenW) {
        return (screenW - panelW(screenW)) / 2;
    }

    public static int panelY(int screenH) {
        return (screenH - panelH(screenH)) / 2;
    }

    public static int contentX(int screenW) {
        return panelX(screenW) + SIDEBAR_W;
    }

    public static int contentY(int screenH) {
        return panelY(screenH) + HEADER_H;
    }

    public static int contentW(int screenW) {
        return panelW(screenW) - SIDEBAR_W;
    }

    public static int contentH(int screenH) {
        return panelH(screenH) - HEADER_H - FOOTER_H;
    }
}
