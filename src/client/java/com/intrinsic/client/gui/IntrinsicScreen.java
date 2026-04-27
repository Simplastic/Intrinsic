package com.intrinsic.client.gui;

import net.minecraft.client.input.MouseButtonEvent;
import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.audio.AudioControl;
import com.intrinsic.client.audio.AudioRegistry;
import com.intrinsic.client.config.IntrinsicConfig;
import com.intrinsic.client.feature.Feature;
import com.intrinsic.client.feature.LightHeatmapFeature;
import com.intrinsic.client.gui.widget.IntSlider;
import com.intrinsic.client.gui.widget.PanelChrome;
import com.intrinsic.client.gui.widget.ThemeColors;
import com.intrinsic.client.gui.widget.VolumeSlider;
import com.intrinsic.client.keybind.KeybindHandle;
import com.intrinsic.client.keybind.Keybinds;
import com.intrinsic.client.resource.TexturePacks;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class IntrinsicScreen extends Screen {
    private enum Section {
        OVERVIEW("Overview", null, false),
        ALL("All Features", null, true),
        HUD("HUD", Feature.Category.HUD, true),
        GAMEPLAY("Gameplay", Feature.Category.GAMEPLAY, true),
        VISUAL("Visual", Feature.Category.VISUAL, true),
        TECHNICAL("Technical", Feature.Category.TECHNICAL, true),
        TEXTURES("Textures", null, false),
        AUDIO("Audio", null, false),
        KEYBINDS("Keybinds", null, false),
        HUD_LAYOUT("HUD Layout\u2026", null, false);

        final String label;
        final Feature.Category category;
        final boolean showsCards;

        Section(String label, Feature.Category category, boolean showsCards) {
            this.label = label;
            this.category = category;
            this.showsCards = showsCards;
        }
    }

    private static final int PANEL_PAD = PanelChrome.PANEL_PAD;
    private static final int PANEL_MAX_W = PanelChrome.PANEL_MAX_W;
    private static final int PANEL_MAX_H = PanelChrome.PANEL_MAX_H;
    private static final int SIDEBAR_W = PanelChrome.SIDEBAR_W;
    private static final int HEADER_H = PanelChrome.HEADER_H;
    private static final int FOOTER_H = PanelChrome.FOOTER_H;
    private static final int CARD_H = PanelChrome.CARD_H;
    private static final int CARD_GAP = PanelChrome.CARD_GAP;
    private static final int PILL_W = PanelChrome.PILL_W;
    private static final int PILL_H = PanelChrome.PILL_H;
    private static final int CHIP_W = 86;
    private static final int CHIP_H = PanelChrome.CHIP_H;
    private static final int SIDEBAR_ROW_H = PanelChrome.SIDEBAR_ROW_H;

    private final Screen parent;
    private Section activeSection = Section.OVERVIEW;
    private String searchQuery = "";
    private int scrollOffset = 0;

    private EditBox searchField;

    private KeybindHandle listeningChip = null;

    private boolean draggingScrollbar = false;
    private int dragStartMouseY = 0;
    private int dragStartScroll = 0;

    private final List<CardHit> cardHits = new ArrayList<>();
    private final List<SidebarHit> sidebarHits = new ArrayList<>();
    private FooterHit doneHit = null;
    private FooterHit resetHit = null;
    private FooterHit applyHit = null;
    private MasterHit masterHit = null;

    // Audio tab state
    private final Map<String, VolumeSlider> audioSliders = new HashMap<>();
    private final List<AudioRowHit> audioRowHits = new ArrayList<>();
    private int[] addPatternHit = null;
    private int[] junkEditHit = null;
    private int[] audioTuningToggleHit = null;
    private VolumeSlider draggingSlider = null;
    private int audioMaxScroll = 0;
    private int audioTrackTop = 0;
    private int audioTrackHeight = 0;

    private IntSlider heatmapRadiusSlider;
    private boolean heatmapSliderVisible = false;
    private IntSlider mobEspRadiusSlider;
    private boolean mobEspSliderVisible = false;

    private final List<TexturePackHit> texturePackHits = new ArrayList<>();

    private Object lastHoverKey = null;
    private long hoverStartMs = 0L;
    private boolean hoverSeenThisFrame = false;
    private List<Component> pendingTooltip = null;
    private int pendingTooltipMx = 0;
    private int pendingTooltipMy = 0;
    private static final long TOOLTIP_DELAY_MS = 600L;

    private record TexturePackHit(int x, int y, int w, int h,
                                   int toggleX, int toggleY,
                                   String id) {}

    private record AudioRowHit(int x, int y, int w, int h,
                                int resetX, int resetY, int resetW, int resetH,
                                String id, boolean custom) {}

    private record CardHit(int x, int y, int w, int h,
                           int toggleX, int toggleY,
                           int chipX, int chipY,
                           Feature feature, KeybindHandle handle) {}

    private record SidebarHit(int x, int y, int w, int h, Section section) {}

    private record FooterHit(int x, int y, int w, int h) {}

    private record MasterHit(int x, int y, int w, int h) {}

    public IntrinsicScreen(Screen parent) {
        super(Component.literal("Intrinsic"));
        this.parent = parent;
    }

    private int panelW() { return Math.min(this.width - PANEL_PAD * 2, PANEL_MAX_W); }
    private int panelH() { return Math.min(this.height - PANEL_PAD, PANEL_MAX_H); }
    private int panelX() { return (this.width - panelW()) / 2; }
    private int panelY() { return (this.height - panelH()) / 2; }
    private int contentX() { return panelX() + SIDEBAR_W; }
    private int contentY() { return panelY() + HEADER_H; }
    private int contentW() { return panelW() - SIDEBAR_W; }
    private int contentH() { return panelH() - HEADER_H - FOOTER_H; }

    @Override
    protected void init() {
        int pX = panelX(), pY = panelY(), pW = panelW();
        int cX = contentX(), cY = contentY();

        int searchW = Math.min(contentW() - 24, 360);
        int searchX = cX + 12;
        int searchY = cY + 8;
        searchField = new EditBox(this.font, searchX, searchY, searchW, 18,
                Component.literal("Search"));
        searchField.setHint(Component.literal("Search\u2026").withStyle(ChatFormatting.DARK_GRAY));
        searchField.setMaxLength(48);
        searchField.setValue(searchQuery);
        searchField.setResponder(text -> {
            if (!text.equals(searchQuery)) {
                searchQuery = text;
                scrollOffset = 0;
            }
        });
        searchField.visible = activeSection.showsCards
                || activeSection == Section.AUDIO
                || activeSection == Section.KEYBINDS
                || activeSection == Section.TEXTURES;
        this.addRenderableWidget(searchField);

        int radius = IntrinsicClient.getConfig().lightHeatmapRadius;
        heatmapRadiusSlider = new IntSlider(0, 0, 160, 14, radius,
                LightHeatmapFeature.minRadius(), LightHeatmapFeature.maxRadius(), " blocks");

        int mobEspR = IntrinsicClient.getConfig().mobEspRadius;
        mobEspRadiusSlider = new IntSlider(0, 0, 160, 14, mobEspR, 4, 96, " blocks");
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        // Override to avoid vanilla blur (crashes if blur was already applied this frame).
        // Our own BACKDROP fill in render() provides the dim; no blur needed.
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, ThemeColors.BACKDROP);
        hoverSeenThisFrame = false;

        int pX = panelX(), pY = panelY(), pW = panelW(), pH = panelH();
        int cX = contentX(), cY = contentY(), cW = contentW(), cH = contentH();

        // Panel outer
        ctx.fill(pX, pY, pX + pW, pY + pH, ThemeColors.PANEL);
        // Sidebar
        ctx.fill(pX, pY, pX + SIDEBAR_W, pY + pH, ThemeColors.SIDEBAR);
        // Header strip (full width, subtle lighter)
        ctx.fill(pX, pY, pX + pW, pY + HEADER_H, ThemeColors.HEADER_BG);
        // Divider header / body
        ctx.fill(pX, pY + HEADER_H, pX + pW, pY + HEADER_H + 1, ThemeColors.DIVIDER);
        // Divider sidebar / content
        ctx.fill(pX + SIDEBAR_W, pY, pX + SIDEBAR_W + 1, pY + pH, ThemeColors.DIVIDER);
        // Footer divider
        ctx.fill(pX + SIDEBAR_W, pY + pH - FOOTER_H, pX + pW, pY + pH - FOOTER_H + 1, ThemeColors.DIVIDER);

        // Title + version in header
        Component titleText = Component.literal("Intrinsic").withStyle(ChatFormatting.BOLD);
        ctx.text(this.font, titleText, pX + 14, pY + 9, ThemeColors.TEXT, true);
        ctx.text(this.font, Component.literal("v1.0").withStyle(ChatFormatting.GRAY), pX + 14 + this.font.width(titleText) + 6, pY + 10, ThemeColors.TEXT_DIM, true);

        // Master HUD pill in header (right side)
        drawMasterHudPill(ctx, mouseX, mouseY);

        // Sidebar items
        drawSidebar(ctx, mouseX, mouseY);

        // Section content
        if (activeSection == Section.OVERVIEW) {
            drawOverview(ctx);
        } else if (activeSection == Section.KEYBINDS) {
            drawKeybinds(ctx, mouseX, mouseY);
        } else if (activeSection == Section.AUDIO) {
            drawAudio(ctx, mouseX, mouseY);
        } else if (activeSection == Section.TEXTURES) {
            drawTextures(ctx, mouseX, mouseY);
        } else if (activeSection.showsCards) {
            drawCards(ctx, mouseX, mouseY);
        } else if (activeSection == Section.HUD_LAYOUT) {
            ctx.centeredText(this.font,
                    Component.literal("Opening layout editor\u2026").withStyle(ChatFormatting.GRAY),
                    cX + cW / 2, cY + cH / 2, ThemeColors.TEXT_DIM);
        }

        // Footer
        drawFooter(ctx, mouseX, mouseY);

        // Vanilla widgets (search field) — render manually to avoid double renderBackground/blur.
        if (searchField != null && searchField.visible) {
            searchField.extractRenderState(ctx, mouseX, mouseY, delta);
        }

        renderPendingTooltip(ctx);

        if (!hoverSeenThisFrame) {
            lastHoverKey = null;
        }
    }

    private void drawMasterHudPill(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        int pX = panelX(), pY = panelY(), pW = panelW();
        boolean on = IntrinsicClient.getConfig().masterHudEnabled;
        String label = on ? "HUDs On" : "HUDs Off";
        int w = 88, h = 20;
        int x = pX + pW - 14 - w;
        int y = pY + (HEADER_H - h) / 2;
        masterHit = new MasterHit(x, y, w, h);
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        drawPill(ctx, x, y, w, h, on, hover, label);
    }

    private void drawSidebar(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        sidebarHits.clear();
        int pX = panelX(), pY = panelY(), pH = panelH();
        int cursorY = pY + HEADER_H + 10;

        Section[] order = {
                Section.OVERVIEW,
                Section.ALL,
                Section.HUD,
                Section.GAMEPLAY,
                Section.VISUAL,
                Section.TECHNICAL,
                Section.TEXTURES,
                Section.AUDIO,
                null, // divider
                Section.KEYBINDS,
                Section.HUD_LAYOUT,
        };

        for (Section s : order) {
            if (s == null) {
                ctx.fill(pX + 12, cursorY + 4, pX + SIDEBAR_W - 12, cursorY + 5, ThemeColors.DIVIDER);
                cursorY += 12;
                continue;
            }
            int y = cursorY;
            int rowX = pX + 6;
            int rowW = SIDEBAR_W - 12;
            boolean active = activeSection == s;
            boolean hover = mouseX >= rowX && mouseX <= rowX + rowW && mouseY >= y && mouseY <= y + SIDEBAR_ROW_H;
            if (active) {
                ctx.fill(rowX, y, rowX + rowW, y + SIDEBAR_ROW_H, ThemeColors.CARD_HOVER);
                ctx.fill(rowX, y, rowX + 3, y + SIDEBAR_ROW_H, ThemeColors.ACCENT);
            } else if (hover) {
                ctx.fill(rowX, y, rowX + rowW, y + SIDEBAR_ROW_H, ThemeColors.SIDEBAR_HOVER);
            }
            int txtColor = active ? ThemeColors.TEXT : (hover ? ThemeColors.TEXT : ThemeColors.TEXT_DIM);
            ctx.text(this.font, Component.literal(s.label), rowX + 12, y + 7, txtColor, true);
            sidebarHits.add(new SidebarHit(rowX, y, rowW, SIDEBAR_ROW_H, s));
            cursorY += SIDEBAR_ROW_H + 2;
        }
    }

    private void drawFooter(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        int pX = panelX(), pY = panelY(), pW = panelW(), pH = panelH();
        int fy = pY + pH - FOOTER_H + 8;

        int doneW = 90, doneH = 20;
        int doneX = pX + pW - 14 - doneW;
        int doneY = fy;
        doneHit = new FooterHit(doneX, doneY, doneW, doneH);
        boolean doneHover = mouseX >= doneX && mouseX <= doneX + doneW && mouseY >= doneY && mouseY <= doneY + doneH;
        drawButton(ctx, doneX, doneY, doneW, doneH, "Done", doneHover, true);

        int resetW = 150, resetH = 20;
        int resetX = pX + SIDEBAR_W + 14;
        int resetY = fy;
        resetHit = new FooterHit(resetX, resetY, resetW, resetH);
        boolean resetHover = mouseX >= resetX && mouseX <= resetX + resetW && mouseY >= resetY && mouseY <= resetY + resetH;
        drawButton(ctx, resetX, resetY, resetW, resetH, "Turn Everything Off", resetHover, false);

        if (activeSection == Section.TEXTURES) {
            int applyW = 90, applyH = 20;
            int applyX = doneX - 8 - applyW;
            int applyY = fy;
            applyHit = new FooterHit(applyX, applyY, applyW, applyH);
            boolean dirty = TexturePacks.isDirty();
            boolean applyHover = mouseX >= applyX && mouseX <= applyX + applyW && mouseY >= applyY && mouseY <= applyY + applyH;
            drawButton(ctx, applyX, applyY, applyW, applyH, dirty ? "Apply" : "Applied", applyHover && dirty, dirty);
        } else {
            applyHit = null;
        }
    }

    private void drawOverview(GuiGraphicsExtractor ctx) {
        int cX = contentX(), cY = contentY(), cW = contentW();
        int x = cX + 20, y = cY + 18;
        int rightCol = cX + cW - 20;

        Component title = Component.literal("Welcome to Intrinsic").withStyle(ChatFormatting.BOLD);
        ctx.text(this.font, title, x, y, ThemeColors.TEXT, true);
        ctx.text(this.font, Component.literal("v1.0").withStyle(ChatFormatting.GRAY),
                x + this.font.width(title) + 6, y + 1, ThemeColors.TEXT_DIM, true);
        y += 14;
        ctx.text(this.font,
                Component.literal("Client-side toolkit for Minecraft 26.1.2 ; HUDs, gameplay tweaks, visual cleanup, technical overlays.")
                        .withStyle(ChatFormatting.GRAY),
                x, y, ThemeColors.TEXT_DIM, true);
        y += 18;

        int hudCount = 0, gpCount = 0, vsCount = 0, tcCount = 0;
        int hudOn = 0, gpOn = 0, vsOn = 0, tcOn = 0;
        for (Feature f : Feature.values()) {
            switch (f.category) {
                case HUD -> { hudCount++; if (f.isEnabled()) hudOn++; }
                case GAMEPLAY -> { gpCount++; if (f.isEnabled()) gpOn++; }
                case VISUAL -> { vsCount++; if (f.isEnabled()) vsOn++; }
                case TECHNICAL -> { tcCount++; if (f.isEnabled()) tcOn++; }
            }
        }
        drawStatRow(ctx, x, rightCol, y, "HUD",       hudOn, hudCount); y += 13;
        drawStatRow(ctx, x, rightCol, y, "Gameplay",  gpOn,  gpCount);  y += 13;
        drawStatRow(ctx, x, rightCol, y, "Visual",    vsOn,  vsCount);  y += 13;
        drawStatRow(ctx, x, rightCol, y, "Technical", tcOn,  tcCount);  y += 18;

        ctx.text(this.font, Component.literal("Quick start").withStyle(ChatFormatting.BOLD), x, y, ThemeColors.TEXT, true);
        y += 12;
        drawKeyHint(ctx, x, y, "Open menu", Keybinds.openMenuHandle()); y += 12;
        drawKeyHint(ctx, x, y, "Toggle all HUDs", Keybinds.handleFor(Feature.MASTER_HUD)); y += 12;

        ctx.text(this.font, Component.literal("Slash commands").withStyle(ChatFormatting.BOLD), x, y, ThemeColors.TEXT, true);
        y += 12;
        drawCmdLine(ctx, x, y, "/inethercoords", "Overworld\u2194Nether coordinate converter."); y += 11;
        drawCmdLine(ctx, x, y, "/ifov",          "Set FOV beyond the vanilla 30\u2013110 clamp."); y += 11;
        drawCmdLine(ctx, x, y, "/ijunk",         "Manage the Junk Drop blacklist."); y += 11;
        drawCmdLine(ctx, x, y, "/iseed",         "Slime-chunk lookup for the loaded seed."); y += 11;
        drawCmdLine(ctx, x, y, "/iwaypoint",     "Add, list, and remove waypoints."); y += 11;
        drawCmdLine(ctx, x, y, "/ispawnsphere",  "Place / clear the spawn-sphere overlay."); y += 18;

        ctx.text(this.font,
                Component.literal("Tip: hover a feature card for ~600 ms to see its full description.")
                        .withStyle(ChatFormatting.GRAY),
                x, y, ThemeColors.TEXT_FAINT, true);
    }

    private void drawStatRow(GuiGraphicsExtractor ctx, int x, int rightCol, int y, String label, int on, int total) {
        ctx.text(this.font, Component.literal(label), x, y, ThemeColors.TEXT, true);
        String right = on + " / " + total + " active";
        int rw = this.font.width(right);
        ctx.text(this.font, Component.literal(right).withStyle(ChatFormatting.GRAY),
                rightCol - rw, y, ThemeColors.TEXT_DIM, true);
    }

    private void drawKeyHint(GuiGraphicsExtractor ctx, int x, int y, String label, KeybindHandle handle) {
        String prefix = label + ": ";
        ctx.text(this.font, Component.literal(prefix).withStyle(ChatFormatting.GRAY),
                x, y, ThemeColors.TEXT_DIM, true);
        String key = (handle == null || handle.keyCode() < 0) ? "Unbound" : handle.label();
        int color = (handle == null || handle.keyCode() < 0) ? ThemeColors.TEXT_FAINT : ThemeColors.TEXT_KEYBIND;
        ctx.text(this.font, Component.literal(key).withStyle(ChatFormatting.BOLD),
                x + this.font.width(prefix), y, color, true);
    }

    private void drawCmdLine(GuiGraphicsExtractor ctx, int x, int y, String cmd, String desc) {
        ctx.text(this.font, Component.literal(cmd).withStyle(ChatFormatting.AQUA),
                x, y, ThemeColors.TEXT_INFO, true);
        ctx.text(this.font, Component.literal("  \u2014  " + desc).withStyle(ChatFormatting.GRAY),
                x + this.font.width(cmd), y, ThemeColors.TEXT_DIM, true);
    }

    private List<Feature> visibleFeatures() {
        String q = searchQuery.trim().toLowerCase(Locale.ROOT);
        List<Feature> out = new ArrayList<>();
        for (Feature f : Feature.values()) {
            if (activeSection.category != null && f.category != activeSection.category) continue;
            if (!q.isEmpty()) {
                String hay = (f.displayName + " " + f.description).toLowerCase(Locale.ROOT);
                if (!hay.contains(q)) continue;
            }
            out.add(f);
        }
        return out;
    }

    private void drawCards(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        cardHits.clear();
        heatmapSliderVisible = false;
        mobEspSliderVisible = false;
        junkEditHit = null;

        int cX = contentX(), cY = contentY(), cW = contentW(), cH = contentH();
        int areaTop = cY + 32;   // below search row
        int areaBottom = cY + cH;
        int areaLeft = cX + 12;
        int areaRight = cX + cW - 12;
        int areaW = areaRight - areaLeft;

        int cols = areaW >= 560 ? 2 : 1;
        int cardW = (areaW - CARD_GAP * (cols - 1)) / cols;

        List<Feature> feats = visibleFeatures();

        int totalRows = (feats.size() + cols - 1) / cols;
        int contentHeight = totalRows * (CARD_H + CARD_GAP);
        int viewH = areaBottom - areaTop;
        int maxScroll = Math.max(0, contentHeight - viewH);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;

        ctx.enableScissor(areaLeft, areaTop, areaRight, areaBottom);

        for (int i = 0; i < feats.size(); i++) {
            int row = i / cols;
            int col = i % cols;
            int x = areaLeft + col * (cardW + CARD_GAP);
            int y = areaTop + row * (CARD_H + CARD_GAP) - scrollOffset;
            if (y + CARD_H < areaTop || y > areaBottom) continue;
            Feature f = feats.get(i);
            drawCard(ctx, mouseX, mouseY, x, y, cardW, CARD_H, f);
        }

        ctx.disableScissor();

        if (feats.isEmpty()) {
            ctx.centeredText(this.font,
                    Component.literal("No features match your search.").withStyle(ChatFormatting.GRAY),
                    cX + cW / 2, areaTop + viewH / 2, ThemeColors.TEXT_DIM);
        }

        // Scrollbar
        if (maxScroll > 0) {
            int barX = areaRight + 2;
            int barW = 4;
            int trackTop = areaTop;
            int trackBot = areaBottom;
            int trackH = trackBot - trackTop;
            int thumbH = Math.max(20, trackH * trackH / contentHeight);
            int thumbY = trackTop + (trackH - thumbH) * scrollOffset / maxScroll;
            ctx.fill(barX, trackTop, barX + barW, trackBot, 0x40000000);
            ctx.fill(barX, thumbY, barX + barW, thumbY + thumbH, ThemeColors.ACCENT_SOFT);
        }
    }

    private void drawCard(GuiGraphicsExtractor ctx, int mouseX, int mouseY,
                           int x, int y, int w, int h, Feature feature) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        boolean active = feature.isEnabled();
        ctx.fill(x, y, x + w, y + h, hover ? ThemeColors.CARD_HOVER : ThemeColors.CARD);
        if (hover) {
            ctx.fill(x, y, x + 2, y + h, ThemeColors.ACCENT);
        } else if (active) {
            ctx.fill(x, y, x + 2, y + h, ThemeColors.ACCENT_SOFT);
        }

        // Title
        ctx.text(this.font, Component.literal(feature.displayName).withStyle(ChatFormatting.BOLD), x + 10, y + 8, ThemeColors.TEXT, true);

        // Description wrapped to roughly 2 lines
        if (!feature.description.isEmpty()) {
            int descWidth = w - 20 - PILL_W - 12;
            List<net.minecraft.util.FormattedCharSequence> wrapped =
                    this.font.split(Component.literal(feature.description), descWidth);
            int limit = Math.min(2, wrapped.size());
            for (int i = 0; i < limit; i++) {
                ctx.text(this.font, wrapped.get(i), x + 10, y + 24 + i * 12, ThemeColors.TEXT_DIM, true);
            }
        }

        boolean on = feature.isEnabled();
        int toggleX = x + w - 12 - PILL_W;
        int toggleY = y + 8;
        boolean toggleHover = mouseX >= toggleX && mouseX <= toggleX + PILL_W
                && mouseY >= toggleY && mouseY <= toggleY + PILL_H;
        drawPill(ctx, toggleX, toggleY, PILL_W, PILL_H, on, toggleHover, on ? "ON" : "OFF");

        KeybindHandle handle = Keybinds.handleFor(feature);
        int chipX = x + w - 12 - CHIP_W;
        int chipY = y + h - 8 - CHIP_H;
        boolean chipHover = mouseX >= chipX && mouseX <= chipX + CHIP_W
                && mouseY >= chipY && mouseY <= chipY + CHIP_H;
        Conflict conflict = conflictFor(handle);
        drawChip(ctx, chipX, chipY, CHIP_W, CHIP_H, handle, chipHover, listeningChip == handle, conflict);

        if (hover) {
            trackHover(feature, Component.literal(feature.displayName).withStyle(ChatFormatting.BOLD),
                    feature.description, conflict, mouseX, mouseY);
        }

        if (feature == Feature.LIGHT_HEATMAP && heatmapRadiusSlider != null) {
            int sliderW = Math.min(160, chipX - (x + 10) - 8);
            if (sliderW >= 80) {
                heatmapRadiusSlider.x = x + 10;
                heatmapRadiusSlider.y = y + h - 8 - heatmapRadiusSlider.h;
                heatmapRadiusSlider.w = sliderW;
                heatmapRadiusSlider.render(ctx, this.font, mouseX, mouseY, feature.isEnabled());
                heatmapSliderVisible = true;
            }
        }

        if (feature == Feature.MOB_ESP && mobEspRadiusSlider != null) {
            int sliderW = Math.min(160, chipX - (x + 10) - 8);
            if (sliderW >= 80) {
                mobEspRadiusSlider.x = x + 10;
                mobEspRadiusSlider.y = y + h - 8 - mobEspRadiusSlider.h;
                mobEspRadiusSlider.w = sliderW;
                mobEspRadiusSlider.render(ctx, this.font, mouseX, mouseY, feature.isEnabled());
                mobEspSliderVisible = true;
            }
        }

        if (feature == Feature.JUNK_DROP) {
            int btnW = 86;
            int btnH = CHIP_H;
            int btnX = x + 10;
            int btnY = y + h - 8 - btnH;
            boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW
                    && mouseY >= btnY && mouseY <= btnY + btnH;
            int bg = btnHover ? ThemeColors.CARD_HOVER : ThemeColors.CHIP;
            ctx.fill(btnX, btnY, btnX + btnW, btnY + btnH, bg);
            String label = "Edit List\u2026";
            int lw = this.font.width(label);
            ctx.text(this.font, Component.literal(label),
                    btnX + (btnW - lw) / 2, btnY + (btnH - 8) / 2,
                    ThemeColors.TEXT, true);
            junkEditHit = new int[]{btnX, btnY, btnW, btnH};
        }

        cardHits.add(new CardHit(x, y, w, h, toggleX, toggleY, chipX, chipY, feature, handle));
    }

    private void drawKeybinds(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        cardHits.clear();
        int cX = contentX(), cY = contentY(), cW = contentW(), cH = contentH();
        int areaTop = cY + 34;
        int areaBottom = cY + cH;
        int areaLeft = cX + 12;
        int areaRight = cX + cW - 12;
        int areaW = areaRight - areaLeft;

        List<KeybindRow> rows = new ArrayList<>();
        rows.add(new KeybindRow("Open Menu", "Toggles this screen.", Keybinds.openMenuHandle(), null));
        for (Feature f : Feature.values()) {
            KeybindHandle h = Keybinds.handleFor(f);
            rows.add(new KeybindRow(f.displayName, f.description, h, f));
        }
        String kq = searchQuery.trim().toLowerCase(Locale.ROOT);
        if (!kq.isEmpty()) {
            rows.removeIf(r -> {
                String n = r.name == null ? "" : r.name.toLowerCase(Locale.ROOT);
                String d = r.description == null ? "" : r.description.toLowerCase(Locale.ROOT);
                return !n.contains(kq) && !d.contains(kq);
            });
        }

        int rowH = 26;
        int gap = 4;
        int contentHeight = rows.size() * (rowH + gap);
        int viewH = areaBottom - areaTop;
        int maxScroll = Math.max(0, contentHeight - viewH);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;

        ctx.enableScissor(areaLeft, areaTop, areaRight, areaBottom);

        for (int i = 0; i < rows.size(); i++) {
            int x = areaLeft;
            int y = areaTop + i * (rowH + gap) - scrollOffset;
            if (y + rowH < areaTop || y > areaBottom) continue;
            KeybindRow r = rows.get(i);
            boolean hover = mouseX >= x && mouseX <= x + areaW
                    && mouseY >= y && mouseY <= y + rowH;
            ctx.fill(x, y, x + areaW, y + rowH, hover ? ThemeColors.CARD_HOVER : ThemeColors.CARD);
            ctx.text(this.font, Component.literal(r.name).withStyle(ChatFormatting.BOLD), x + 10, y + 4, ThemeColors.TEXT, true);
            if (r.description != null && !r.description.isEmpty()) {
                int descMaxW = areaW - 20 - CHIP_W - 12;
                net.minecraft.util.FormattedCharSequence line = this.font.split(
                        Component.literal(r.description), descMaxW).stream().findFirst().orElse(null);
                if (line != null) {
                    ctx.text(this.font, line, x + 10, y + 16, ThemeColors.TEXT_DIM, true);
                }
            }
            int chipX = x + areaW - 10 - CHIP_W;
            int chipY = y + (rowH - CHIP_H) / 2;
            boolean chipHover = mouseX >= chipX && mouseX <= chipX + CHIP_W
                    && mouseY >= chipY && mouseY <= chipY + CHIP_H;
            Conflict rowConflict = conflictFor(r.handle);
            drawChip(ctx, chipX, chipY, CHIP_W, CHIP_H, r.handle, chipHover, listeningChip == r.handle, rowConflict);
            if (hover) {
                trackHover(r.handle, Component.literal(r.name).withStyle(ChatFormatting.BOLD),
                        r.description, rowConflict, mouseX, mouseY);
            }
            cardHits.add(new CardHit(x, y, areaW, rowH, -1, -1, chipX, chipY, r.feature, r.handle));
        }

        ctx.disableScissor();

        if (maxScroll > 0) {
            int barX = areaRight + 2;
            int barW = 4;
            int trackH = areaBottom - areaTop;
            int thumbH = Math.max(20, trackH * trackH / contentHeight);
            int thumbY = areaTop + (trackH - thumbH) * scrollOffset / maxScroll;
            ctx.fill(barX, areaTop, barX + barW, areaBottom, 0x40000000);
            ctx.fill(barX, thumbY, barX + barW, thumbY + thumbH, ThemeColors.ACCENT_SOFT);
        }
    }

    private record KeybindRow(String name, String description, KeybindHandle handle, Feature feature) {}

    private void drawTextures(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        texturePackHits.clear();
        int cX = contentX(), cY = contentY(), cW = contentW(), cH = contentH();

        if (TexturePacks.ALL.isEmpty()) {
            int cx = cX + cW / 2;
            int cy = cY + cH / 2;
            ctx.centeredText(this.font,
                    Component.literal("Work in progress").withStyle(ChatFormatting.BOLD),
                    cx, cy - 10, ThemeColors.TEXT);
            ctx.centeredText(this.font,
                    Component.literal("Curated texture packs are coming soon.").withStyle(ChatFormatting.GRAY),
                    cx, cy + 6, ThemeColors.TEXT_DIM);
            return;
        }

        int headerTop = cY + 34;
        int areaLeft = cX + 12;
        int areaRight = cX + cW - 12;
        int areaW = areaRight - areaLeft;

        ctx.text(this.font,
                Component.literal("Copyright").withStyle(ChatFormatting.BOLD),
                areaLeft, headerTop, ThemeColors.TEXT, true);
        ctx.text(this.font,
                Component.literal("Texture packs from VanillaTweaks - vanillatweaks.net")
                        .withStyle(ChatFormatting.GRAY),
                areaLeft, headerTop + 11, ThemeColors.TEXT_DIM, true);

        int areaTop = headerTop + 28;
        int areaBottom = cY + cH;

        String q = searchQuery.trim().toLowerCase(Locale.ROOT);
        List<TexturePacks.Pack> packs = new ArrayList<>();
        for (TexturePacks.Pack p : TexturePacks.ALL) {
            if (!q.isEmpty()) {
                String name = p.displayName().toLowerCase(Locale.ROOT);
                String desc = p.description().toLowerCase(Locale.ROOT);
                if (!name.contains(q) && !desc.contains(q)) continue;
            }
            packs.add(p);
        }

        int rowH = 58;
        int gap = 6;
        int contentHeight = packs.size() * (rowH + gap);
        int viewH = areaBottom - areaTop;
        int maxScroll = Math.max(0, contentHeight - viewH);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;

        ctx.enableScissor(areaLeft, areaTop, areaRight, areaBottom);
        for (int i = 0; i < packs.size(); i++) {
            int x = areaLeft;
            int y = areaTop + i * (rowH + gap) - scrollOffset;
            if (y + rowH < areaTop || y > areaBottom) continue;
            TexturePacks.Pack p = packs.get(i);
            boolean hover = mouseX >= x && mouseX <= x + areaW
                    && mouseY >= y && mouseY <= y + rowH;
            boolean on = TexturePacks.isEnabled(p.id());
            ctx.fill(x, y, x + areaW, y + rowH, hover ? ThemeColors.CARD_HOVER : ThemeColors.CARD);
            if (on) ctx.fill(x, y, x + 2, y + rowH, ThemeColors.ACCENT_SOFT);

            int thumbSize = 44;
            int thumbX = x + 8;
            int thumbY = y + (rowH - thumbSize) / 2;
            ctx.fill(thumbX - 1, thumbY - 1, thumbX + thumbSize + 1, thumbY + thumbSize + 1, ThemeColors.DIVIDER);
            ctx.fill(thumbX, thumbY, thumbX + thumbSize, thumbY + thumbSize,
                    on ? ThemeColors.ACCENT_SOFT : ThemeColors.CHIP);
            String initial = p.displayName().substring(0, 1).toUpperCase(Locale.ROOT);
            int iw = this.font.width(initial);
            ctx.text(this.font, Component.literal(initial).withStyle(ChatFormatting.BOLD), thumbX + (thumbSize - iw) / 2, thumbY + (thumbSize - 8) / 2, ThemeColors.TEXT, true);

            int textX = thumbX + thumbSize + 10;
            ctx.text(this.font, Component.literal(p.displayName()).withStyle(ChatFormatting.BOLD), textX, y + 10, ThemeColors.TEXT, true);

            int descMaxW = areaW - (textX - x) - PILL_W - 20;
            List<net.minecraft.util.FormattedCharSequence> wrapped =
                    this.font.split(Component.literal(p.description()), descMaxW);
            int limit = Math.min(2, wrapped.size());
            for (int j = 0; j < limit; j++) {
                ctx.text(this.font, wrapped.get(j), textX, y + 24 + j * 11, ThemeColors.TEXT_DIM, true);
            }

            int toggleX = x + areaW - 12 - PILL_W;
            int toggleY = y + (rowH - PILL_H) / 2;
            boolean toggleHover = mouseX >= toggleX && mouseX <= toggleX + PILL_W
                    && mouseY >= toggleY && mouseY <= toggleY + PILL_H;
            drawPill(ctx, toggleX, toggleY, PILL_W, PILL_H, on, toggleHover, on ? "ON" : "OFF");

            texturePackHits.add(new TexturePackHit(x, y, areaW, rowH, toggleX, toggleY, p.id()));
        }
        ctx.disableScissor();

        if (packs.isEmpty()) {
            ctx.centeredText(this.font,
                    Component.literal("No texture packs match your search.").withStyle(ChatFormatting.GRAY),
                    cX + cW / 2, areaTop + viewH / 2, ThemeColors.TEXT_DIM);
        }

        if (maxScroll > 0) {
            int barX = areaRight + 2;
            int barW = 4;
            int trackH = areaBottom - areaTop;
            int thumbH = Math.max(20, trackH * trackH / contentHeight);
            int thumbY = areaTop + (trackH - thumbH) * scrollOffset / maxScroll;
            ctx.fill(barX, areaTop, barX + barW, areaBottom, 0x40000000);
            ctx.fill(barX, thumbY, barX + barW, thumbY + thumbH, ThemeColors.ACCENT_SOFT);
        }
    }

    private VolumeSlider sliderFor(String id, double stored) {
        VolumeSlider s = audioSliders.get(id);
        if (s == null) {
            s = new VolumeSlider(0, 0, 200, 14, stored, 0.0, 2.0);
            audioSliders.put(id, s);
        } else {
            if (!s.dragging && Math.abs(s.value - stored) > 0.001) s.value = stored;
        }
        return s;
    }

    private void drawAudio(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        audioRowHits.clear();
        addPatternHit = null;
        audioTuningToggleHit = null;
        cardHits.clear();

        int cX = contentX(), cY = contentY(), cW = contentW(), cH = contentH();
        int areaLeft = cX + 12;
        int areaRight = cX + cW - 12;
        int areaW = areaRight - areaLeft;

        boolean tuningOn = IntrinsicClient.getConfig().audioTuning;

        // Master toggle pill (fixed chrome, above scrollable list)
        int toggleY = cY + 8;
        int toggleH = 20;
        int pillW = 64;
        int pillX = areaRight - pillW;
        int labelY = toggleY + (toggleH - 8) / 2;
        ctx.text(this.font, Component.literal("Audio Tuning").withStyle(ChatFormatting.BOLD),
                areaLeft, labelY, ThemeColors.TEXT, true);
        ctx.text(this.font,
                Component.literal("Master enable/disable for all audio category multipliers.")
                        .withStyle(ChatFormatting.GRAY),
                areaLeft, toggleY + toggleH + 2, ThemeColors.TEXT_DIM, true);
        boolean pillHover = mouseX >= pillX && mouseX <= pillX + pillW
                && mouseY >= toggleY && mouseY <= toggleY + toggleH;
        drawPill(ctx, pillX, toggleY, pillW, toggleH, tuningOn, pillHover, tuningOn ? "ON" : "OFF");
        audioTuningToggleHit = new int[]{pillX, toggleY, pillW, toggleH};

        int bannerY = toggleY + toggleH + 14;
        int bannerH = 18;
        if (!tuningOn) {
            ctx.fill(areaLeft, bannerY, areaLeft + areaW, bannerY + bannerH, 0xFF3A2A1A);
            ctx.fill(areaLeft, bannerY, areaLeft + areaW, bannerY + 1, 0xFFD08A3A);
            ctx.fill(areaLeft, bannerY + bannerH - 1, areaLeft + areaW, bannerY + bannerH, 0xFFD08A3A);
            ctx.text(this.font,
                    Component.literal("AUDIO TUNING DISABLED ; sliders below have no effect.")
                            .withStyle(ChatFormatting.YELLOW),
                    areaLeft + 8, bannerY + 5, 0xFFFFC872, true);
        }

        int areaTop = (tuningOn ? bannerY : bannerY + bannerH + 4) + 4;
        int areaBottom = cY + cH;
        int rowH = 48;
        int gap = 6;

        List<AudioControl> builtins = new ArrayList<>(AudioRegistry.builtins().values());
        List<IntrinsicConfig.CustomAudioPattern> customs = AudioRegistry.customPatterns();

        String q = searchQuery.trim().toLowerCase(Locale.ROOT);
        List<AudioControl> filtered = new ArrayList<>();
        for (AudioControl c : builtins) {
            if (!q.isEmpty()) {
                String hay = (c.displayName + " " + c.description).toLowerCase(Locale.ROOT);
                if (!hay.contains(q)) continue;
            }
            filtered.add(c);
        }

        int headerH = 22;
        int contentHeight = headerH + filtered.size() * (rowH + gap)
                + headerH + customs.size() * (rowH + gap) + headerH;
        int viewH = areaBottom - areaTop;
        int maxScroll = Math.max(0, contentHeight - viewH);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;

        this.audioMaxScroll = maxScroll;
        this.audioTrackTop = areaTop;
        this.audioTrackHeight = viewH;

        ctx.enableScissor(areaLeft, areaTop, areaRight, areaBottom);

        int y = areaTop - scrollOffset;

        // Section header: Built-in
        ctx.text(this.font, Component.literal("Built-in categories").withStyle(ChatFormatting.BOLD), areaLeft, y + 6, ThemeColors.TEXT, true);
        y += headerH;

        for (AudioControl c : filtered) {
            drawAudioRow(ctx, mouseX, mouseY, areaLeft, y, areaW, rowH, c, tuningOn);
            y += rowH + gap;
        }

        // Section header: Custom
        ctx.text(this.font, Component.literal("Custom patterns").withStyle(ChatFormatting.BOLD), areaLeft, y + 4, ThemeColors.TEXT, true);
        ctx.text(this.font, Component.literal("Match by sound id prefix (e.g. block.chest.)").withStyle(ChatFormatting.GRAY), areaLeft, y + 16, ThemeColors.TEXT_DIM, true);
        y += headerH + 6;

        for (IntrinsicConfig.CustomAudioPattern p : customs) {
            drawAudioCustomRow(ctx, mouseX, mouseY, areaLeft, y, areaW, rowH, p, tuningOn);
            y += rowH + gap;
        }

        int addBtnW = 160, addBtnH = 22;
        int addBtnX = areaLeft;
        int addBtnY = y + 4;
        boolean addHover = mouseX >= addBtnX && mouseX <= addBtnX + addBtnW
                && mouseY >= addBtnY && mouseY <= addBtnY + addBtnH;
        int addBg = addHover ? ThemeColors.ACCENT_HOVER : ThemeColors.ACCENT;
        ctx.fill(addBtnX, addBtnY, addBtnX + addBtnW, addBtnY + addBtnH, addBg);
        int addLabelW = this.font.width("+ Add pattern");
        ctx.text(this.font, Component.literal("+ Add pattern").withStyle(ChatFormatting.BOLD), addBtnX + (addBtnW - addLabelW) / 2, addBtnY + (addBtnH - 8) / 2, ThemeColors.TEXT, true);
        addPatternHit = new int[]{addBtnX, addBtnY, addBtnW, addBtnH};

        ctx.disableScissor();

        if (maxScroll > 0) {
            int barX = areaRight + 2;
            int barW = 4;
            int trackH = areaBottom - areaTop;
            int thumbH = Math.max(20, trackH * trackH / contentHeight);
            int thumbY = areaTop + (trackH - thumbH) * scrollOffset / maxScroll;
            ctx.fill(barX, areaTop, barX + barW, areaBottom, 0x40000000);
            ctx.fill(barX, thumbY, barX + barW, thumbY + thumbH, ThemeColors.ACCENT_SOFT);
        }
    }

    private void drawAudioRow(GuiGraphicsExtractor ctx, int mouseX, int mouseY,
                               int x, int y, int w, int h, AudioControl c, boolean enabled) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        ctx.fill(x, y, x + w, y + h, hover ? ThemeColors.CARD_HOVER : ThemeColors.CARD);
        if (hover) ctx.fill(x, y, x + 2, y + h, ThemeColors.ACCENT);

        ctx.text(this.font, Component.literal(c.displayName).withStyle(ChatFormatting.BOLD), x + 10, y + 6, ThemeColors.TEXT, true);
        if (!c.description.isEmpty()) {
            ctx.text(this.font, Component.literal(c.description).withStyle(ChatFormatting.GRAY), x + 10, y + 22, ThemeColors.TEXT_DIM, true);
        }

        double stored = AudioRegistry.getVolume(c.id);
        VolumeSlider s = sliderFor(c.id, stored);
        int sliderW = 200;
        int sliderH = 14;
        int sliderX = x + w - 10 - sliderW - 60;
        int sliderY = y + h - 8 - sliderH;
        s.x = sliderX; s.y = sliderY; s.w = sliderW; s.h = sliderH;
        s.render(ctx, this.font, mouseX, mouseY, enabled);

        // Reset chip
        int resetW = 54, resetH = CHIP_H;
        int resetX = x + w - 10 - resetW;
        int resetY = sliderY + (sliderH - resetH) / 2;
        boolean resetHover = mouseX >= resetX && mouseX <= resetX + resetW
                && mouseY >= resetY && mouseY <= resetY + resetH;
        int bg = resetHover ? ThemeColors.CHIP_HOVER : ThemeColors.CHIP;
        int border = resetHover ? ThemeColors.ACCENT_SOFT : ThemeColors.DIVIDER;
        ctx.fill(resetX, resetY, resetX + resetW, resetY + resetH, bg);
        ctx.fill(resetX, resetY, resetX + resetW, resetY + 1, border);
        ctx.fill(resetX, resetY + resetH - 1, resetX + resetW, resetY + resetH, border);
        int tw = this.font.width("Reset");
        ctx.text(this.font, Component.literal("Reset"), resetX + (resetW - tw) / 2, resetY + (resetH - 8) / 2 + 1, ThemeColors.TEXT, true);

        audioRowHits.add(new AudioRowHit(x, y, w, h, resetX, resetY, resetW, resetH, c.id, false));
    }

    private void drawAudioCustomRow(GuiGraphicsExtractor ctx, int mouseX, int mouseY,
                                      int x, int y, int w, int h,
                                      IntrinsicConfig.CustomAudioPattern p, boolean enabled) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        ctx.fill(x, y, x + w, y + h, hover ? ThemeColors.CARD_HOVER : ThemeColors.CARD);
        if (hover) ctx.fill(x, y, x + 2, y + h, ThemeColors.ACCENT);

        String name = (p.displayName == null || p.displayName.isEmpty()) ? p.pattern : p.displayName;
        ctx.text(this.font, Component.literal(name).withStyle(ChatFormatting.BOLD), x + 10, y + 6, ThemeColors.TEXT, true);
        ctx.text(this.font, Component.literal("prefix: " + (p.pattern == null ? "" : p.pattern)).withStyle(ChatFormatting.GRAY), x + 10, y + 22, ThemeColors.TEXT_DIM, true);

        String sid = "custom:" + (p.id == null ? p.pattern : p.id);
        VolumeSlider s = sliderFor(sid, p.volume);
        int sliderW = 200;
        int sliderH = 14;
        int sliderX = x + w - 10 - sliderW - 60;
        int sliderY = y + h - 8 - sliderH;
        s.x = sliderX; s.y = sliderY; s.w = sliderW; s.h = sliderH;
        s.render(ctx, this.font, mouseX, mouseY, enabled);

        int delW = 54, delH = CHIP_H;
        int delX = x + w - 10 - delW;
        int delY = sliderY + (sliderH - delH) / 2;
        boolean delHover = mouseX >= delX && mouseX <= delX + delW
                && mouseY >= delY && mouseY <= delY + delH;
        int bg = delHover ? 0xFF7A2323 : 0xFF3A2020;
        int border = delHover ? 0xFFD05050 : 0xFF623030;
        ctx.fill(delX, delY, delX + delW, delY + delH, bg);
        ctx.fill(delX, delY, delX + delW, delY + 1, border);
        ctx.fill(delX, delY + delH - 1, delX + delW, delY + delH, border);
        int tw = this.font.width("Remove");
        ctx.text(this.font, Component.literal("Remove"), delX + (delW - tw) / 2, delY + (delH - 8) / 2 + 1, ThemeColors.TEXT, true);

        audioRowHits.add(new AudioRowHit(x, y, w, h, delX, delY, delW, delH, p.id, true));
    }

    private boolean handleAudioClick(double mx, double my, int button) {
        if (button != 0) return false;
        if (audioTuningToggleHit != null
                && mx >= audioTuningToggleHit[0] && mx <= audioTuningToggleHit[0] + audioTuningToggleHit[2]
                && my >= audioTuningToggleHit[1] && my <= audioTuningToggleHit[1] + audioTuningToggleHit[3]) {
            Feature.AUDIO_TUNING.toggle();
            IntrinsicClient.getConfig().save();
            return true;
        }
        if (addPatternHit != null
                && mx >= addPatternHit[0] && mx <= addPatternHit[0] + addPatternHit[2]
                && my >= addPatternHit[1] && my <= addPatternHit[1] + addPatternHit[3]) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new CustomAudioEditorScreen(this, null));
            }
            return true;
        }
        boolean tuningOn = IntrinsicClient.getConfig().audioTuning;
        // Try sliders first
        if (tuningOn) {
            for (AudioRowHit rh : audioRowHits) {
                VolumeSlider s = rh.custom
                        ? audioSliders.get("custom:" + rh.id)
                        : audioSliders.get(rh.id);
                if (s != null && s.mouseClicked(mx, my, 0)) {
                    draggingSlider = s;
                    persistSlider(rh);
                    return true;
                }
            }
        }
        for (AudioRowHit rh : audioRowHits) {
            if (mx >= rh.resetX && mx <= rh.resetX + rh.resetW
                    && my >= rh.resetY && my <= rh.resetY + rh.resetH) {
                if (rh.custom) {
                    AudioRegistry.removeCustomPattern(rh.id);
                    audioSliders.remove("custom:" + rh.id);
                } else {
                    AudioRegistry.resetVolume(rh.id);
                    VolumeSlider s = audioSliders.get(rh.id);
                    if (s != null) s.value = 1.0;
                }
                IntrinsicClient.getConfig().save();
                return true;
            }
        }
        return false;
    }

    private void persistSlider(AudioRowHit rh) {
        VolumeSlider s = rh.custom
                ? audioSliders.get("custom:" + rh.id)
                : audioSliders.get(rh.id);
        if (s == null) return;
        if (rh.custom) {
            for (IntrinsicConfig.CustomAudioPattern p : AudioRegistry.customPatterns()) {
                if (rh.id != null && rh.id.equals(p.id)) { p.volume = s.value; break; }
            }
        } else {
            AudioRegistry.setVolume(rh.id, s.value);
        }
    }

    private void drawPill(GuiGraphicsExtractor ctx, int x, int y, int w, int h, boolean on, boolean hover, String label) {
        int bg = on ? (hover ? ThemeColors.ON : ThemeColors.ON_DARK)
                    : (hover ? ThemeColors.OFF : ThemeColors.OFF_DARK);
        ctx.fill(x, y, x + w, y + h, bg);
        int border = on ? ThemeColors.ON : ThemeColors.OFF;
        ctx.fill(x, y, x + w, y + 1, border);
        ctx.fill(x, y + h - 1, x + w, y + h, border);
        int tw = this.font.width(label);
        ctx.text(this.font, Component.literal(label).withStyle(ChatFormatting.BOLD), x + (w - tw) / 2, y + (h - 8) / 2, ThemeColors.TEXT, true);
    }

    private void drawButton(GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label,
                             boolean hover, boolean primary) {
        int bg = primary ? (hover ? ThemeColors.ACCENT : ThemeColors.ACCENT_DEEP)
                         : (hover ? ThemeColors.CARD_HOVER : ThemeColors.CARD);
        ctx.fill(x, y, x + w, y + h, bg);
        int border = primary ? ThemeColors.ACCENT : ThemeColors.DIVIDER;
        ctx.fill(x, y, x + w, y + 1, border);
        ctx.fill(x, y + h - 1, x + w, y + h, border);
        int tw = this.font.width(label);
        ctx.text(this.font, Component.literal(label), x + (w - tw) / 2, y + (h - 8) / 2, ThemeColors.TEXT, true);
    }

    private void drawChip(GuiGraphicsExtractor ctx, int x, int y, int w, int h,
                           KeybindHandle handle, boolean hover, boolean listening, Conflict conflict) {
        int bg;
        int border;
        String label;
        if (listening) {
            bg = ThemeColors.LISTENING_BG;
            border = ThemeColors.CHIP_LISTENING;
            label = "Press key\u2026";
        } else if (conflict != null && !conflict.none) {
            bg = hover ? 0xFF4A2828 : ThemeColors.WARN_BG;
            border = ThemeColors.WARN;
            label = handle == null ? "—" : handle.label();
        } else {
            bg = hover ? ThemeColors.CHIP_HOVER : ThemeColors.CHIP;
            border = hover ? ThemeColors.ACCENT_SOFT : ThemeColors.DIVIDER;
            label = handle == null ? "—" : handle.label();
        }
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.fill(x, y, x + w, y + 1, border);
        ctx.fill(x, y + h - 1, x + w, y + h, border);

        String prefix = "Key: ";
        int prefixW = this.font.width(prefix);
        int labelW = this.font.width(label);
        int totalW = prefixW + labelW;
        int startX = x + (w - totalW) / 2;
        int textY = y + (h - 8) / 2 + 1;
        ctx.text(this.font, Component.literal(prefix), startX, textY, ThemeColors.TEXT_FAINT, true);
        ctx.text(this.font, Component.literal(label).withStyle(ChatFormatting.BOLD), startX + prefixW, textY, ThemeColors.TEXT, true);

        if (conflict != null && !conflict.none) {
            ctx.text(this.font, Component.literal("!").withStyle(ChatFormatting.BOLD),
                    x + w - 6, textY, ThemeColors.WARN, true);
        }
    }

    private int scrollbarMax() {
        if (activeSection == Section.AUDIO) return audioMaxScroll;
        if (activeSection == Section.KEYBINDS) {
            int rowH = 26, gap = 4;
            int rows = Feature.values().length + 1;
            int viewH = contentH() - 16;
            return Math.max(0, rows * (rowH + gap) - viewH);
        }
        if (activeSection == Section.TEXTURES) {
            int rowH = 58, gap = 6;
            int rows = TexturePacks.ALL.size();
            int viewH = contentH() - 34;
            return Math.max(0, rows * (rowH + gap) - viewH);
        }
        if (!activeSection.showsCards) return 0;
        List<Feature> feats = visibleFeatures();
        int cols = (contentW() - 24) >= 560 ? 2 : 1;
        int rows = (feats.size() + cols - 1) / cols;
        int contentHeight = rows * (CARD_H + CARD_GAP);
        int viewH = contentH() - 32;
        return Math.max(0, contentHeight - viewH);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();

        if (super.mouseClicked(click, doubled)) return true;

        if (button == 0 && masterHit != null && inRect(mx, my, masterHit.x, masterHit.y, masterHit.w, masterHit.h)) {
            IntrinsicConfig cfg = IntrinsicClient.getConfig();
            cfg.masterHudEnabled = !cfg.masterHudEnabled;
            cfg.save();
            return true;
        }
        if (button == 0 && doneHit != null && inRect(mx, my, doneHit.x, doneHit.y, doneHit.w, doneHit.h)) {
            onClose();
            return true;
        }
        if (button == 0 && resetHit != null && inRect(mx, my, resetHit.x, resetHit.y, resetHit.w, resetHit.h)) {
            turnEverythingOff();
            return true;
        }
        if (button == 0 && applyHit != null && inRect(mx, my, applyHit.x, applyHit.y, applyHit.w, applyHit.h)) {
            if (TexturePacks.isDirty()) {
                TexturePacks.applyNow(this.minecraft);
            }
            return true;
        }
        for (SidebarHit sh : sidebarHits) {
            if (button == 0 && inRect(mx, my, sh.x, sh.y, sh.w, sh.h)) {
                if (sh.section == Section.HUD_LAYOUT) {
                    this.minecraft.setScreen(new HudLayoutScreen(this));
                    return true;
                }
                if (activeSection != sh.section) {
                    activeSection = sh.section;
                    scrollOffset = 0;
                    searchQuery = "";
                    listeningChip = null;
                    if (searchField != null) {
                        searchField.setValue("");
                        searchField.visible = sh.section.showsCards
                                || sh.section == Section.AUDIO
                                || sh.section == Section.KEYBINDS
                                || sh.section == Section.TEXTURES;
                    }
                    if (sh.section == Section.TEXTURES) {
                        TexturePacks.captureSnapshot();
                    }
                }
                return true;
            }
        }
        if (activeSection == Section.AUDIO) {
            if (handleAudioClick(mx, my, button)) return true;
        }
        if (activeSection == Section.TEXTURES && button == 0) {
            for (TexturePackHit th : texturePackHits) {
                if (inRect(mx, my, th.toggleX, th.toggleY, PILL_W, PILL_H)
                        || inRect(mx, my, th.x, th.y, th.w, th.h)) {
                    TexturePacks.setEnabled(th.id, !TexturePacks.isEnabled(th.id));
                    return true;
                }
            }
        }
        if (heatmapSliderVisible && heatmapRadiusSlider != null
                && heatmapRadiusSlider.mouseClicked(mx, my, button)) {
            IntrinsicClient.getConfig().lightHeatmapRadius = heatmapRadiusSlider.value;
            return true;
        }
        if (mobEspSliderVisible && mobEspRadiusSlider != null
                && mobEspRadiusSlider.mouseClicked(mx, my, button)) {
            IntrinsicClient.getConfig().mobEspRadius = mobEspRadiusSlider.value;
            return true;
        }
        if (junkEditHit != null && button == 0
                && inRect(mx, my, junkEditHit[0], junkEditHit[1], junkEditHit[2], junkEditHit[3])) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new JunkBlacklistEditorScreen(this));
            }
            listeningChip = null;
            return true;
        }
        for (CardHit ch : cardHits) {
            if (button == 0 && ch.toggleX >= 0
                    && inRect(mx, my, ch.toggleX, ch.toggleY, PILL_W, PILL_H)) {
                if (ch.feature != null) {
                    ch.feature.toggle();
                    IntrinsicClient.getConfig().save();
                }
                listeningChip = null;
                return true;
            }
            if (button == 0 && inRect(mx, my, ch.chipX, ch.chipY, CHIP_W, CHIP_H)) {
                if (listeningChip == ch.handle) {
                    listeningChip = null;
                } else {
                    listeningChip = ch.handle;
                    if (searchField != null) searchField.setFocused(false);
                    this.setFocused(null);
                }
                return true;
            }
        }

        // Scrollbar (rough hit on right edge of content area)
        int cX = contentX(), cY = contentY(), cW = contentW(), cH = contentH();
        int barX = cX + cW - 12 - 12 + 14;
        int barTop = activeSection == Section.AUDIO ? audioTrackTop : cY;
        int barBottom = activeSection == Section.AUDIO ? (audioTrackTop + audioTrackHeight) : (cY + cH);
        if (button == 0 && mx >= barX && mx <= barX + 4 && my >= barTop && my <= barBottom
                && scrollbarMax() > 0) {
            draggingScrollbar = true;
            dragStartMouseY = (int) my;
            dragStartScroll = scrollOffset;
            return true;
        }

        // Clicking outside any listening chip cancels listening.
        listeningChip = null;
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (heatmapRadiusSlider != null && heatmapRadiusSlider.dragging && click.button() == 0) {
            heatmapRadiusSlider.mouseDragged(click.x(), click.y(), 0);
            IntrinsicClient.getConfig().lightHeatmapRadius = heatmapRadiusSlider.value;
            return true;
        }
        if (mobEspRadiusSlider != null && mobEspRadiusSlider.dragging && click.button() == 0) {
            mobEspRadiusSlider.mouseDragged(click.x(), click.y(), 0);
            IntrinsicClient.getConfig().mobEspRadius = mobEspRadiusSlider.value;
            return true;
        }
        if (draggingSlider != null && click.button() == 0) {
            draggingSlider.mouseDragged(click.x(), click.y(), 0);
            for (AudioRowHit rh : audioRowHits) {
                VolumeSlider match = rh.custom
                        ? audioSliders.get("custom:" + rh.id)
                        : audioSliders.get(rh.id);
                if (match == draggingSlider) { persistSlider(rh); break; }
            }
            return true;
        }
        if (draggingScrollbar && click.button() == 0) {
            int max = scrollbarMax();
            if (max > 0) {
                int trackH = activeSection == Section.AUDIO ? audioTrackHeight : (contentH() - 32);
                int dy = (int) click.y() - dragStartMouseY;
                scrollOffset = Math.max(0, Math.min(max, dragStartScroll + dy * max / Math.max(1, trackH)));
            }
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (heatmapRadiusSlider != null && heatmapRadiusSlider.dragging && click.button() == 0) {
            heatmapRadiusSlider.mouseReleased(click.x(), click.y(), 0);
            IntrinsicClient.getConfig().lightHeatmapRadius = heatmapRadiusSlider.value;
            IntrinsicClient.getConfig().save();
            return true;
        }
        if (mobEspRadiusSlider != null && mobEspRadiusSlider.dragging && click.button() == 0) {
            mobEspRadiusSlider.mouseReleased(click.x(), click.y(), 0);
            IntrinsicClient.getConfig().mobEspRadius = mobEspRadiusSlider.value;
            IntrinsicClient.getConfig().save();
            return true;
        }
        if (draggingSlider != null && click.button() == 0) {
            draggingSlider.mouseReleased(click.x(), click.y(), 0);
            draggingSlider = null;
            IntrinsicClient.getConfig().save();
            return true;
        }
        if (draggingScrollbar && click.button() == 0) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (heatmapSliderVisible && heatmapRadiusSlider != null) {
            boolean fine = isShiftDown();
            if (heatmapRadiusSlider.mouseScrolled(mouseX, mouseY, verticalAmount, fine)) {
                IntrinsicClient.getConfig().lightHeatmapRadius = heatmapRadiusSlider.value;
                IntrinsicClient.getConfig().save();
                return true;
            }
        }
        if (mobEspSliderVisible && mobEspRadiusSlider != null) {
            boolean fine = isShiftDown();
            if (mobEspRadiusSlider.mouseScrolled(mouseX, mouseY, verticalAmount, fine)) {
                IntrinsicClient.getConfig().mobEspRadius = mobEspRadiusSlider.value;
                IntrinsicClient.getConfig().save();
                return true;
            }
        }
        if (activeSection == Section.AUDIO && IntrinsicClient.getConfig().audioTuning) {
            boolean fine = isShiftDown();
            for (AudioRowHit rh : audioRowHits) {
                VolumeSlider s = rh.custom
                        ? audioSliders.get("custom:" + rh.id)
                        : audioSliders.get(rh.id);
                if (s != null && s.mouseScrolled(mouseX, mouseY, verticalAmount, fine)) {
                    persistSlider(rh);
                    IntrinsicClient.getConfig().save();
                    return true;
                }
            }
        }
        int cX = contentX(), cY = contentY(), cW = contentW(), cH = contentH();
        if (mouseX >= cX && mouseX <= cX + cW && mouseY >= cY && mouseY <= cY + cH) {
            int max = scrollbarMax();
            scrollOffset = Math.max(0, Math.min(max, scrollOffset - (int) (verticalAmount * 18)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (listeningChip != null) {
            int key = input.key();
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                listeningChip.clear();
            } else {
                listeningChip.setKeyCode(key);
            }
            listeningChip = null;
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        // While listening for a keybind, swallow the char so it doesn't leak into the search box.
        if (listeningChip != null) return true;
        return super.charTyped(event);
    }

    private boolean inRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private record Conflict(boolean none, boolean dup, String otherLabel) {
        static final Conflict NONE = new Conflict(true, false, null);
    }

    private Conflict conflictFor(KeybindHandle handle) {
        if (handle == null) return Conflict.NONE;
        int code = handle.keyCode();
        if (code < 0) return Conflict.NONE;

        for (Feature f : Feature.values()) {
            KeybindHandle other = Keybinds.handleFor(f);
            if (other == handle) continue;
            if (other.keyCode() == code && !handle.displayName().equals(other.displayName())) {
                return new Conflict(false, true, "Intrinsic / " + other.displayName());
            }
        }
        KeybindHandle openMenu = Keybinds.openMenuHandle();
        if (openMenu != handle && openMenu.keyCode() == code) {
            return new Conflict(false, true, "Intrinsic / " + openMenu.displayName());
        }

        Minecraft mc = this.minecraft;
        if (mc != null) {
            for (KeyMapping km : mc.options.keyMappings) {
                String name = km.getName();
                if (name == null || name.startsWith("key.intrinsic.")) continue;
                InputConstants.Key key = InputConstants.getKey(km.saveString());
                if (key == InputConstants.UNKNOWN) continue;
                if (key.getValue() == code) {
                    return new Conflict(false, false, "Vanilla / " + I18n.get(name));
                }
            }
        }
        return Conflict.NONE;
    }

    private void trackHover(Object key, Component title, String description, Conflict conflict, int mouseX, int mouseY) {
        if (key == null) return;
        hoverSeenThisFrame = true;
        if (!key.equals(lastHoverKey)) {
            lastHoverKey = key;
            hoverStartMs = System.currentTimeMillis();
            return;
        }
        if (System.currentTimeMillis() - hoverStartMs < TOOLTIP_DELAY_MS) return;

        List<Component> lines = new ArrayList<>();
        lines.add(title);
        if (description != null && !description.isEmpty()) {
            for (net.minecraft.util.FormattedCharSequence line : this.font.split(
                    Component.literal(description).withStyle(ChatFormatting.GRAY), 240)) {
                lines.add(toComponent(line));
            }
        }
        if (conflict != null && !conflict.none) {
            lines.add(Component.literal("Conflicts with " + conflict.otherLabel).withStyle(ChatFormatting.RED));
        }
        pendingTooltip = lines;
        pendingTooltipMx = mouseX;
        pendingTooltipMy = mouseY;
    }

    private static Component toComponent(net.minecraft.util.FormattedCharSequence seq) {
        StringBuilder sb = new StringBuilder();
        seq.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return Component.literal(sb.toString()).withStyle(ChatFormatting.GRAY);
    }

    private void renderPendingTooltip(GuiGraphicsExtractor ctx) {
        if (pendingTooltip == null || pendingTooltip.isEmpty()) return;
        int padding = 6;
        int lineH = 11;
        int maxW = 0;
        for (Component c : pendingTooltip) maxW = Math.max(maxW, this.font.width(c));
        int boxW = maxW + padding * 2;
        int boxH = pendingTooltip.size() * lineH + padding * 2 - 2;

        int bx = pendingTooltipMx + 12;
        int by = pendingTooltipMy + 12;
        if (bx + boxW > this.width - 4) bx = this.width - 4 - boxW;
        if (by + boxH > this.height - 4) by = pendingTooltipMy - 4 - boxH;
        if (bx < 4) bx = 4;
        if (by < 4) by = 4;

        ctx.fill(bx, by, bx + boxW, by + boxH, ThemeColors.TOOLTIP_BG);
        ctx.fill(bx, by, bx + boxW, by + 1, ThemeColors.TOOLTIP_BORDER);
        ctx.fill(bx, by + boxH - 1, bx + boxW, by + boxH, ThemeColors.TOOLTIP_BORDER);
        ctx.fill(bx, by, bx + 1, by + boxH, ThemeColors.TOOLTIP_BORDER);
        ctx.fill(bx + boxW - 1, by, bx + boxW, by + boxH, ThemeColors.TOOLTIP_BORDER);

        for (int i = 0; i < pendingTooltip.size(); i++) {
            ctx.text(this.font, pendingTooltip.get(i), bx + padding, by + padding + i * lineH, ThemeColors.TEXT, true);
        }

        pendingTooltip = null;
    }

    private boolean isShiftDown() {
        long w = this.minecraft.getWindow().handle();
        return GLFW.glfwGetKey(w, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(w, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    private void turnEverythingOff() {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        cfg.masterHudEnabled = false;
        for (Feature f : Feature.values()) f.setEnabled(false);
        cfg.save();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        IntrinsicClient.getConfig().save();
        if (TexturePacks.isDirty()) {
            TexturePacks.applyNow(this.minecraft);
        }
        this.minecraft.setScreen(parent);
    }
}
