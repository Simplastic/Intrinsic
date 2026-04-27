package com.intrinsic.client.gui;

import net.minecraft.client.input.MouseButtonEvent;
import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.gui.widget.PanelChrome;
import com.intrinsic.client.gui.widget.ThemeColors;
import com.intrinsic.client.hud.HudPositions;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HudLayoutScreen extends Screen {
    private enum Group {
        ALL("All", null),
        INFO("Info", new String[]{"coordinates", "biome", "clock", "dayCounter", "lightLevel"}),
        STATUS("Status", new String[]{"durability", "potionEffects", "itemCounter", "deathCoords", "entityInfo"}),
        WARNINGS("Warnings", new String[]{"armorWarning", "hungerWarning", "durabilityWarning"}),
        TECHNICAL("Technical", new String[]{"fps", "ping", "tps", "signalStrength", "slimeChunk", "slimeChunkMap", "chunkBorders", "blockUpdate", "villagerTrades"});

        final String label;
        final String[] ids;
        Group(String label, String[] ids) { this.label = label; this.ids = ids; }

        boolean contains(String id) {
            if (ids == null) return true;
            for (String s : ids) if (s.equals(id)) return true;
            return false;
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
    private static final int CHIP_W = 52;
    private static final int CHIP_H = PanelChrome.CHIP_H;
    private static final int SIDEBAR_ROW_H = PanelChrome.SIDEBAR_ROW_H;

    private final Screen parent;
    private Group activeGroup = Group.ALL;
    private int scrollOffset = 0;
    private String searchQuery = "";
    private EditBox searchField;

    private String draggingId = null;      // id being live-moved
    private String movingId = null;         // id locked into move mode (drag overlay)
    private int dragAnchorMouseX = 0;
    private int dragAnchorMouseY = 0;
    private int dragStartPosX = 0;
    private int dragStartPosY = 0;

    private boolean draggingScrollbar = false;
    private int dragStartScroll = 0;
    private int dragStartScrollMouseY = 0;

    private final List<CardHit> cardHits = new ArrayList<>();
    private final List<SidebarHit> sidebarHits = new ArrayList<>();
    private FooterHit doneHit = null;
    private FooterHit resetHit = null;
    private FooterHit exitMoveHit = null;

    private record CardHit(int x, int y, int w, int h,
                           int pillX, int pillY,
                           int resetX, int resetY,
                           int xChipX, int xChipY,
                           int yChipX, int yChipY,
                           String id) {}

    private record SidebarHit(int x, int y, int w, int h, Group group) {}

    private record FooterHit(int x, int y, int w, int h) {}

    public HudLayoutScreen(Screen parent) {
        super(Component.literal("Intrinsic \u2014 HUD Layout"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int pX = panelX(), pY = panelY();
        int cX = contentX();
        int searchX = cX + 12;
        int searchY = pY + 10;
        int searchW = Math.min(260, contentW() - 24);
        searchField = new EditBox(this.font, searchX, searchY, searchW, 18,
                Component.literal("Search"));
        searchField.setHint(Component.literal("Search HUDs\u2026").withStyle(ChatFormatting.DARK_GRAY));
        searchField.setMaxLength(48);
        searchField.setValue(searchQuery);
        searchField.setResponder(text -> {
            if (!text.equals(searchQuery)) {
                searchQuery = text;
                scrollOffset = 0;
            }
        });
        this.addRenderableWidget(searchField);
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
    public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        // Own backdrop — no vanilla blur.
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        if (movingId != null) {
            renderMoveOverlay(ctx, mouseX, mouseY);
            return;
        }
        renderPanel(ctx, mouseX, mouseY);
    }

    private void renderPanel(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        ctx.fill(0, 0, this.width, this.height, ThemeColors.BACKDROP);

        int pX = panelX(), pY = panelY(), pW = panelW(), pH = panelH();

        ctx.fill(pX, pY, pX + pW, pY + pH, ThemeColors.PANEL);
        ctx.fill(pX, pY, pX + SIDEBAR_W, pY + pH, ThemeColors.SIDEBAR);
        ctx.fill(pX, pY, pX + pW, pY + HEADER_H, ThemeColors.HEADER_BG);
        ctx.fill(pX, pY + HEADER_H, pX + pW, pY + HEADER_H + 1, ThemeColors.DIVIDER);
        ctx.fill(pX + SIDEBAR_W, pY, pX + SIDEBAR_W + 1, pY + pH, ThemeColors.DIVIDER);
        ctx.fill(pX + SIDEBAR_W, pY + pH - FOOTER_H, pX + pW, pY + pH - FOOTER_H + 1, ThemeColors.DIVIDER);

        Component titleText = Component.literal("HUD Layout").withStyle(ChatFormatting.BOLD);
        ctx.text(this.font, titleText, pX + 14, pY + 9, ThemeColors.TEXT, true);
        ctx.text(this.font, Component.literal("Drag to reposition").withStyle(ChatFormatting.GRAY), pX + 14 + this.font.width(titleText) + 8, pY + 10, ThemeColors.TEXT_DIM, true);

        drawSidebar(ctx, mouseX, mouseY);
        drawCards(ctx, mouseX, mouseY);
        drawFooter(ctx, mouseX, mouseY);
    }

    private void drawSidebar(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        sidebarHits.clear();
        int pX = panelX(), pY = panelY();
        int cursorY = pY + HEADER_H + 10;

        for (Group g : Group.values()) {
            int y = cursorY;
            int rowX = pX + 6;
            int rowW = SIDEBAR_W - 12;
            boolean active = activeGroup == g;
            boolean hover = mouseX >= rowX && mouseX <= rowX + rowW && mouseY >= y && mouseY <= y + SIDEBAR_ROW_H;
            if (active) {
                ctx.fill(rowX, y, rowX + rowW, y + SIDEBAR_ROW_H, ThemeColors.CARD_HOVER);
                ctx.fill(rowX, y, rowX + 3, y + SIDEBAR_ROW_H, ThemeColors.ACCENT);
            } else if (hover) {
                ctx.fill(rowX, y, rowX + rowW, y + SIDEBAR_ROW_H, ThemeColors.SIDEBAR_HOVER);
            }
            int txtColor = active ? ThemeColors.TEXT : (hover ? ThemeColors.TEXT : ThemeColors.TEXT_DIM);
            ctx.text(this.font, Component.literal(g.label), rowX + 12, y + 7, txtColor, true);
            sidebarHits.add(new SidebarHit(rowX, y, rowW, SIDEBAR_ROW_H, g));
            cursorY += SIDEBAR_ROW_H + 2;
        }

        // Little helper block at the bottom of the sidebar
        int hintY = pY + panelH() - FOOTER_H - 60;
        ctx.text(this.font, Component.literal("Tips").withStyle(ChatFormatting.BOLD), pX + 12, hintY, ThemeColors.TEXT_DIM, true);
        ctx.text(this.font, Component.literal("\u2022 Move: drag in world"), pX + 12, hintY + 12, ThemeColors.TEXT_FAINT, true);
        ctx.text(this.font, Component.literal("\u2022 Arrows: nudge \u00b11"), pX + 12, hintY + 22, ThemeColors.TEXT_FAINT, true);
        ctx.text(this.font, Component.literal("\u2022 Shift: \u00b110"), pX + 12, hintY + 32, ThemeColors.TEXT_FAINT, true);
    }

    private List<Map.Entry<String, HudPositions.Anchor>> visibleAnchors() {
        List<Map.Entry<String, HudPositions.Anchor>> out = new ArrayList<>();
        String q = searchQuery.trim().toLowerCase(java.util.Locale.ROOT);
        for (Map.Entry<String, HudPositions.Anchor> e : HudPositions.all().entrySet()) {
            if (!activeGroup.contains(e.getKey())) continue;
            if (!HudPositions.isHudActive(e.getKey())) continue;
            if (!q.isEmpty()) {
                String name = e.getValue().displayName == null ? "" : e.getValue().displayName.toLowerCase(java.util.Locale.ROOT);
                String desc = HudDescriptions.get(e.getKey()).toLowerCase(java.util.Locale.ROOT);
                if (!name.contains(q) && !desc.contains(q)) continue;
            }
            out.add(e);
        }
        return out;
    }

    private void drawCards(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        cardHits.clear();

        int cX = contentX(), cY = contentY(), cW = contentW(), cH = contentH();
        int areaTop = cY + 8;
        int areaBottom = cY + cH;
        int areaLeft = cX + 12;
        int areaRight = cX + cW - 12;
        int areaW = areaRight - areaLeft;

        int cols = areaW >= 560 ? 2 : 1;
        int cardW = (areaW - CARD_GAP * (cols - 1)) / cols;

        List<Map.Entry<String, HudPositions.Anchor>> items = visibleAnchors();
        int totalRows = (items.size() + cols - 1) / cols;
        int contentHeight = totalRows * (CARD_H + CARD_GAP);
        int viewH = areaBottom - areaTop;
        int maxScroll = Math.max(0, contentHeight - viewH);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;

        ctx.enableScissor(areaLeft, areaTop, areaRight, areaBottom);

        for (int i = 0; i < items.size(); i++) {
            int row = i / cols;
            int col = i % cols;
            int x = areaLeft + col * (cardW + CARD_GAP);
            int y = areaTop + row * (CARD_H + CARD_GAP) - scrollOffset;
            if (y + CARD_H < areaTop || y > areaBottom) continue;
            drawCard(ctx, mouseX, mouseY, x, y, cardW, CARD_H, items.get(i).getKey(), items.get(i).getValue());
        }

        ctx.disableScissor();

        if (items.isEmpty()) {
            boolean anyActiveOverall = HudPositions.all().keySet().stream()
                    .anyMatch(HudPositions::isHudActive);
            String msg = anyActiveOverall
                    ? "No HUDs in this group."
                    : "Enable HUDs in the main menu first.";
            ctx.centeredText(this.font,
                    Component.literal(msg).withStyle(ChatFormatting.GRAY),
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

    private void drawCard(GuiGraphicsExtractor ctx, int mouseX, int mouseY,
                           int x, int y, int w, int h, String id, HudPositions.Anchor a) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        ctx.fill(x, y, x + w, y + h, hover ? ThemeColors.CARD_HOVER : ThemeColors.CARD);
        if (hover) ctx.fill(x, y, x + 2, y + h, ThemeColors.ACCENT);

        ctx.text(this.font, Component.literal(a.displayName).withStyle(ChatFormatting.BOLD), x + 10, y + 8, ThemeColors.TEXT, true);

        String desc = HudDescriptions.get(id);
        if (!desc.isEmpty()) {
            int descMaxW = w - 20;
            net.minecraft.util.FormattedCharSequence line = this.font.split(
                    Component.literal(desc), descMaxW).stream().findFirst().orElse(null);
            if (line != null) {
                ctx.text(this.font, line, x + 10, y + 22, ThemeColors.TEXT_DIM, true);
            }
        }

        // X / Y chips
        int sw = this.minecraft != null ? this.minecraft.getWindow().getGuiScaledWidth() : 854;
        int sh = this.minecraft != null ? this.minecraft.getWindow().getGuiScaledHeight() : 480;
        int curX = HudPositions.x(id, sw);
        int curY = HudPositions.y(id, sh);

        int chipsY = y + 40;
        int xChipX = x + 10;
        int xChipY = chipsY;
        boolean xHover = mouseX >= xChipX && mouseX <= xChipX + CHIP_W
                && mouseY >= xChipY && mouseY <= xChipY + CHIP_H;
        drawMiniChip(ctx, xChipX, xChipY, CHIP_W, CHIP_H, "X: " + curX, xHover);

        int yChipX = xChipX + CHIP_W + 4;
        int yChipY = chipsY;
        boolean yHover = mouseX >= yChipX && mouseX <= yChipX + CHIP_W
                && mouseY >= yChipY && mouseY <= yChipY + CHIP_H;
        drawMiniChip(ctx, yChipX, yChipY, CHIP_W, CHIP_H, "Y: " + curY, yHover);

        // Overridden indicator
        boolean overridden = HudPositions.isOverridden(id);
        if (overridden) {
            int tag = 0xFF3B82F6;
            int dotX = x + w - 12;
            int dotY = y + 10;
            ctx.fill(dotX, dotY, dotX + 4, dotY + 4, tag);
        }

        // Move pill (primary action)
        int pillX = x + w - 12 - PILL_W;
        int pillY = y + h - 10 - PILL_H;
        boolean pillHover = mouseX >= pillX && mouseX <= pillX + PILL_W
                && mouseY >= pillY && mouseY <= pillY + PILL_H;
        drawAccentPill(ctx, pillX, pillY, PILL_W, PILL_H, "Move", pillHover);

        // Reset chip (left of pill)
        int resetW = 54;
        int resetX = pillX - 8 - resetW;
        int resetY = pillY + (PILL_H - CHIP_H) / 2;
        boolean resetHover = mouseX >= resetX && mouseX <= resetX + resetW
                && mouseY >= resetY && mouseY <= resetY + CHIP_H;
        drawMiniChip(ctx, resetX, resetY, resetW, CHIP_H, overridden ? "Reset" : "Default", resetHover);

        cardHits.add(new CardHit(x, y, w, h, pillX, pillY, resetX, resetY,
                xChipX, xChipY, yChipX, yChipY, id));
    }

    private void drawMiniChip(GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, boolean hover) {
        int bg = hover ? ThemeColors.CHIP_HOVER : ThemeColors.CHIP;
        int border = hover ? ThemeColors.ACCENT_SOFT : ThemeColors.DIVIDER;
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.fill(x, y, x + w, y + 1, border);
        ctx.fill(x, y + h - 1, x + w, y + h, border);
        int tw = this.font.width(label);
        int tx = x + (w - tw) / 2;
        int ty = y + (h - 8) / 2 + 1;
        ctx.text(this.font, Component.literal(label), tx, ty, ThemeColors.TEXT, true);
    }

    private void drawAccentPill(GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, boolean hover) {
        int bg = hover ? ThemeColors.ACCENT : ThemeColors.ACCENT_DEEP;
        int border = ThemeColors.ACCENT;
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.fill(x, y, x + w, y + 1, border);
        ctx.fill(x, y + h - 1, x + w, y + h, border);
        int tw = this.font.width(label);
        ctx.text(this.font, Component.literal(label).withStyle(ChatFormatting.BOLD), x + (w - tw) / 2, y + (h - 8) / 2, ThemeColors.TEXT, true);
    }

    private void drawFooter(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        int pX = panelX(), pY = panelY(), pW = panelW(), pH = panelH();
        int fy = pY + pH - FOOTER_H + 8;

        int doneW = 90, doneH = 20;
        int doneX = pX + pW - 14 - doneW;
        doneHit = new FooterHit(doneX, fy, doneW, doneH);
        boolean doneHover = mouseX >= doneX && mouseX <= doneX + doneW && mouseY >= fy && mouseY <= fy + doneH;
        drawButton(ctx, doneX, fy, doneW, doneH, "Done", doneHover, true);

        int resetW = 120, resetH = 20;
        int resetX = pX + SIDEBAR_W + 14;
        resetHit = new FooterHit(resetX, fy, resetW, resetH);
        boolean resetHover = mouseX >= resetX && mouseX <= resetX + resetW && mouseY >= fy && mouseY <= fy + resetH;
        drawButton(ctx, resetX, fy, resetW, resetH, "Reset All", resetHover, false);
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

    // ======= Move mode overlay =======

    private void renderMoveOverlay(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        // Dim world lightly — the HUDs were already rendered by vanilla then dispatcher
        // before this screen drew. Draw a darker wash only at the edges to keep HUDs readable.
        ctx.fill(0, 0, this.width, this.height, 0x66000000);

        // Draw ghost rectangles of every active HUD (only the ones that would actually render)
        for (Map.Entry<String, HudPositions.Anchor> e : HudPositions.all().entrySet()) {
            String id = e.getKey();
            if (!HudPositions.isHudActive(id)) continue;
            HudPositions.Anchor a = e.getValue();
            int ax = HudPositions.x(id, this.width);
            int ay = HudPositions.y(id, this.height);
            int w = Math.max(20, a.width);
            int h = Math.max(8, a.height);
            boolean isTarget = id.equals(movingId);
            int border = isTarget ? 0xFFFFAA00 : 0x55FFFFFF;
            int fill = isTarget ? 0x40FFAA00 : 0x10FFFFFF;
            ctx.fill(ax, ay, ax + w, ay + h, fill);
            ctx.fill(ax, ay, ax + w, ay + 1, border);
            ctx.fill(ax, ay + h - 1, ax + w, ay + h, border);
            ctx.fill(ax, ay, ax + 1, ay + h, border);
            ctx.fill(ax + w - 1, ay, ax + w, ay + h, border);
            if (isTarget) {
                ctx.text(this.font, Component.literal(a.displayName).withStyle(ChatFormatting.BOLD), ax + 2, ay - 10, ThemeColors.TEXT_KEYBIND, true);
                ctx.text(this.font, Component.literal("[" + ax + ", " + ay + "]").withStyle(ChatFormatting.GRAY), ax + 2, ay + h + 2, 0xFFBBBBBB, true);
            }
        }

        // Help banner at top center
        String line1 = "Moving: " + HudPositions.get(movingId).displayName;
        String line2 = "Drag to move \u2022 Arrows \u00b11 \u2022 Shift+Arrows \u00b110 \u2022 ESC / Done = commit";
        int bannerW = Math.max(this.font.width(line1), this.font.width(line2)) + 24;
        int bannerH = 30;
        int bannerX = this.width / 2 - bannerW / 2;
        int bannerY = 8;
        ctx.fill(bannerX, bannerY, bannerX + bannerW, bannerY + bannerH, 0xE0101218);
        ctx.fill(bannerX, bannerY, bannerX + bannerW, bannerY + 1, ThemeColors.ACCENT);
        ctx.fill(bannerX, bannerY + bannerH - 1, bannerX + bannerW, bannerY + bannerH, ThemeColors.ACCENT);
        ctx.centeredText(this.font, Component.literal(line1).withStyle(ChatFormatting.BOLD),
                this.width / 2, bannerY + 4, ThemeColors.TEXT);
        ctx.centeredText(this.font, Component.literal(line2).withStyle(ChatFormatting.GRAY),
                this.width / 2, bannerY + 16, ThemeColors.TEXT_DIM);

        // Done button (top-right)
        int btnW = 70, btnH = 18;
        int btnX = this.width - btnW - 10;
        int btnY = 10;
        exitMoveHit = new FooterHit(btnX, btnY, btnW, btnH);
        boolean hover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        drawButton(ctx, btnX, btnY, btnW, btnH, "Done", hover, true);
    }

    // ======= Input =======

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();

        if (movingId != null) {
            if (button == 0 && exitMoveHit != null
                    && mx >= exitMoveHit.x && mx <= exitMoveHit.x + exitMoveHit.w
                    && my >= exitMoveHit.y && my <= exitMoveHit.y + exitMoveHit.h) {
                commitMove();
                return true;
            }
            if (button == 0) {
                HudPositions.Anchor a = HudPositions.get(movingId);
                int cx = HudPositions.x(movingId, this.width);
                int cy = HudPositions.y(movingId, this.height);
                int w = Math.max(20, a.width);
                int h = Math.max(8, a.height);
                if (mx >= cx && mx <= cx + w && my >= cy && my <= cy + h) {
                    draggingId = movingId;
                    dragAnchorMouseX = (int) mx;
                    dragAnchorMouseY = (int) my;
                    dragStartPosX = cx;
                    dragStartPosY = cy;
                    return true;
                }
                // Click outside target area — still commit but stay in move mode
                return true;
            }
            return false;
        }

        if (super.mouseClicked(click, doubled)) return true;

        if (button == 0 && doneHit != null && inRect(mx, my, doneHit.x, doneHit.y, doneHit.w, doneHit.h)) {
            onClose();
            return true;
        }
        if (button == 0 && resetHit != null && inRect(mx, my, resetHit.x, resetHit.y, resetHit.w, resetHit.h)) {
            for (String id : new ArrayList<>(HudPositions.all().keySet())) HudPositions.reset(id);
            IntrinsicClient.getConfig().save();
            return true;
        }
        for (SidebarHit sh : sidebarHits) {
            if (button == 0 && inRect(mx, my, sh.x, sh.y, sh.w, sh.h)) {
                if (activeGroup != sh.group) {
                    activeGroup = sh.group;
                    scrollOffset = 0;
                }
                return true;
            }
        }
        for (CardHit ch : cardHits) {
            if (button == 0 && inRect(mx, my, ch.pillX, ch.pillY, PILL_W, PILL_H)) {
                enterMoveMode(ch.id);
                return true;
            }
            if (button == 0 && inRect(mx, my, ch.resetX, ch.resetY, 54, CHIP_H)) {
                HudPositions.reset(ch.id);
                IntrinsicClient.getConfig().save();
                return true;
            }
            // Scroll-to-edit via wheel happens in mouseScrolled when hovered on chips
        }

        // Scrollbar drag
        int cX = contentX(), cY = contentY(), cW = contentW(), cH = contentH();
        int barX = cX + cW - 12 - 2;
        if (button == 0 && mx >= barX && mx <= barX + 6 && my >= cY && my <= cY + cH) {
            draggingScrollbar = true;
            dragStartScrollMouseY = (int) my;
            dragStartScroll = scrollOffset;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (movingId != null && draggingId != null && click.button() == 0) {
            int dx = (int) click.x() - dragAnchorMouseX;
            int dy = (int) click.y() - dragAnchorMouseY;
            int nx = clampPos(dragStartPosX + dx, this.width);
            int ny = clampPos(dragStartPosY + dy, this.height);
            HudPositions.setPosition(draggingId, nx, ny);
            return true;
        }
        if (draggingScrollbar && click.button() == 0) {
            int max = scrollbarMax();
            if (max > 0) {
                int trackH = contentH() - 16;
                int d = (int) click.y() - dragStartScrollMouseY;
                scrollOffset = Math.max(0, Math.min(max, dragStartScroll + d * max / Math.max(1, trackH)));
            }
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (draggingId != null && click.button() == 0) {
            draggingId = null;
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
        if (movingId == null) {
            int cX = contentX(), cY = contentY(), cW = contentW(), cH = contentH();
            if (mouseX >= cX && mouseX <= cX + cW && mouseY >= cY && mouseY <= cY + cH) {
                int max = scrollbarMax();
                scrollOffset = Math.max(0, Math.min(max, scrollOffset - (int) (verticalAmount * 18)));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        int key = input.key();
        if (movingId != null) {
            int step = isShiftDown() ? 10 : 1;
            int cx = HudPositions.x(movingId, this.width);
            int cy = HudPositions.y(movingId, this.height);
            switch (key) {
                case GLFW.GLFW_KEY_LEFT -> HudPositions.setPosition(movingId, clampPos(cx - step, this.width), cy);
                case GLFW.GLFW_KEY_RIGHT -> HudPositions.setPosition(movingId, clampPos(cx + step, this.width), cy);
                case GLFW.GLFW_KEY_UP -> HudPositions.setPosition(movingId, cx, clampPos(cy - step, this.height));
                case GLFW.GLFW_KEY_DOWN -> HudPositions.setPosition(movingId, cx, clampPos(cy + step, this.height));
                case GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_ENTER -> { commitMove(); return true; }
                default -> { return super.keyPressed(input); }
            }
            IntrinsicClient.getConfig().save();
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(input);
    }

    private int clampPos(int v, int bound) {
        return Math.max(0, Math.min(bound - 4, v));
    }

    private void enterMoveMode(String id) {
        movingId = id;
        draggingId = null;
    }

    private void commitMove() {
        movingId = null;
        draggingId = null;
        IntrinsicClient.getConfig().save();
    }

    private int scrollbarMax() {
        int cW = contentW();
        int cols = (cW - 24) >= 560 ? 2 : 1;
        int items = visibleAnchors().size();
        int rows = (items + cols - 1) / cols;
        int contentHeight = rows * (CARD_H + CARD_GAP);
        int viewH = contentH() - 16;
        return Math.max(0, contentHeight - viewH);
    }

    private boolean inRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private boolean isShiftDown() {
        long w = this.minecraft.getWindow().handle();
        return GLFW.glfwGetKey(w, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(w, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        IntrinsicClient.getConfig().save();
        this.minecraft.setScreen(parent);
    }
}
