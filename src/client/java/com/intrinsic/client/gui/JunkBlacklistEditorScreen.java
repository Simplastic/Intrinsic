package com.intrinsic.client.gui;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import com.intrinsic.client.feature.JunkDropFeature;
import com.intrinsic.client.gui.widget.ThemeColors;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class JunkBlacklistEditorScreen extends Screen {
    private final Screen parent;

    private EditBox addField;
    private String validationError = null;

    private int panelX, panelY, panelW, panelH;
    private int listTop, listBottom;
    private int scrollOffset = 0;

    private static final int ROW_H = 18;
    private static final int ROW_GAP = 2;

    public JunkBlacklistEditorScreen(Screen parent) {
        super(Component.literal("Junk Blacklist"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelW = 420;
        panelH = 340;
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        int fieldW = panelW - 40 - 80 - 10;
        addField = new EditBox(this.font, panelX + 20, panelY + panelH - 70, fieldW, 20,
                Component.literal("Item id"));
        addField.setMaxLength(64);
        addField.setHint(Component.literal("e.g. minecraft:cobblestone or cobblestone")
                .withStyle(ChatFormatting.DARK_GRAY));
        this.addRenderableWidget(addField);

        listTop = panelY + 44;
        listBottom = panelY + panelH - 80;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        super.extractRenderState(ctx, mouseX, mouseY, delta);
        ctx.fill(0, 0, this.width, this.height, ThemeColors.BACKDROP);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, ThemeColors.PANEL);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 1, ThemeColors.ACCENT);

        ctx.text(this.font,
                Component.literal("Junk Blacklist").withStyle(ChatFormatting.BOLD),
                panelX + 20, panelY + 14, ThemeColors.TEXT, true);

        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        int count = cfg.junkBlacklist.size();
        ctx.text(this.font,
                Component.literal(count + " item" + (count == 1 ? "" : "s"))
                        .withStyle(ChatFormatting.GRAY),
                panelX + panelW - 20 - this.font.width(count + " items"),
                panelY + 16, ThemeColors.TEXT_DIM, true);

        drawList(ctx, mouseX, mouseY);

        ctx.text(this.font,
                Component.literal("Add item").withStyle(ChatFormatting.GRAY),
                panelX + 20, panelY + panelH - 82, ThemeColors.TEXT_DIM, true);
        addField.extractRenderState(ctx, mouseX, mouseY, delta);
        int addBtnX = panelX + panelW - 20 - 80;
        int addBtnY = panelY + panelH - 70;
        drawButton(ctx, "Add", addBtnX, addBtnY, 80, 20, mouseX, mouseY, true);

        if (validationError != null) {
            ctx.text(this.font,
                    Component.literal(validationError).withStyle(ChatFormatting.RED),
                    panelX + 20, panelY + panelH - 46, 0xFFFF6666, true);
        }

        int doneX = panelX + panelW - 20 - 120;
        int doneY = panelY + panelH - 30;
        drawButton(ctx, "Done", doneX, doneY, 120, 20, mouseX, mouseY, false);

        int clearX = panelX + 20;
        int clearY = panelY + panelH - 30;
        drawButton(ctx, "Clear all", clearX, clearY, 100, 20, mouseX, mouseY, false);

        super.extractRenderState(ctx, mouseX, mouseY, delta);
    }

    private void drawList(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        int listX = panelX + 20;
        int listW = panelW - 40;
        ctx.fill(listX, listTop, listX + listW, listBottom, ThemeColors.CARD);

        int viewH = listBottom - listTop;
        int contentH = cfg.junkBlacklist.size() * (ROW_H + ROW_GAP);
        int maxScroll = Math.max(0, contentH - viewH);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;

        ctx.enableScissor(listX, listTop, listX + listW, listBottom);

        if (cfg.junkBlacklist.isEmpty()) {
            String msg = "Blacklist is empty. Add an item below.";
            int tw = this.font.width(msg);
            ctx.text(this.font,
                    Component.literal(msg).withStyle(ChatFormatting.GRAY),
                    listX + (listW - tw) / 2, listTop + viewH / 2 - 4,
                    ThemeColors.TEXT_DIM, true);
        }

        for (int i = 0; i < cfg.junkBlacklist.size(); i++) {
            String id = cfg.junkBlacklist.get(i);
            int rowX = listX + 4;
            int rowY = listTop + i * (ROW_H + ROW_GAP) - scrollOffset;
            int rowW = listW - 8;
            if (rowY + ROW_H < listTop || rowY > listBottom) continue;

            boolean rowHover = mouseX >= rowX && mouseX <= rowX + rowW
                    && mouseY >= rowY && mouseY <= rowY + ROW_H;
            ctx.fill(rowX, rowY, rowX + rowW, rowY + ROW_H,
                    rowHover ? ThemeColors.CARD_HOVER : ThemeColors.CHIP);

            ctx.text(this.font, Component.literal(id), rowX + 6, rowY + 5,
                    ThemeColors.TEXT, true);

            int rmW = 60;
            int rmX = rowX + rowW - rmW - 3;
            int rmY = rowY + 2;
            boolean rmHover = mouseX >= rmX && mouseX <= rmX + rmW
                    && mouseY >= rmY && mouseY <= rmY + ROW_H - 4;
            int rmBg = rmHover ? 0xFFB8463C : 0xFF7A3A33;
            ctx.fill(rmX, rmY, rmX + rmW, rmY + ROW_H - 4, rmBg);
            String label = "Remove";
            int lw = this.font.width(label);
            ctx.text(this.font, Component.literal(label),
                    rmX + (rmW - lw) / 2, rmY + 3, ThemeColors.TEXT, true);
        }

        ctx.disableScissor();

        if (maxScroll > 0) {
            int barX = listX + listW + 2;
            int barW = 4;
            int trackH = listBottom - listTop;
            int thumbH = Math.max(16, trackH * trackH / contentH);
            int thumbY = listTop + (trackH - thumbH) * scrollOffset / maxScroll;
            ctx.fill(barX, listTop, barX + barW, listBottom, 0x40000000);
            ctx.fill(barX, thumbY, barX + barW, thumbY + thumbH, ThemeColors.ACCENT_SOFT);
        }
    }

    private void drawButton(GuiGraphicsExtractor ctx, String label, int x, int y, int w, int h,
                             int mouseX, int mouseY, boolean primary) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = primary
                ? (hover ? ThemeColors.ACCENT_HOVER : ThemeColors.ACCENT)
                : (hover ? ThemeColors.CARD_HOVER : ThemeColors.CHIP);
        ctx.fill(x, y, x + w, y + h, bg);
        int tw = this.font.width(label);
        ctx.text(this.font, Component.literal(label), x + (w - tw) / 2, y + (h - 8) / 2,
                ThemeColors.TEXT, true);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();

        int doneX = panelX + panelW - 20 - 120;
        int doneY = panelY + panelH - 30;
        if (button == 0 && inside(mx, my, doneX, doneY, 120, 20)) { onClose(); return true; }

        int clearX = panelX + 20;
        int clearY = panelY + panelH - 30;
        if (button == 0 && inside(mx, my, clearX, clearY, 100, 20)) {
            IntrinsicConfig cfg = IntrinsicClient.getConfig();
            cfg.junkBlacklist.clear();
            cfg.save();
            validationError = null;
            return true;
        }

        int addBtnX = panelX + panelW - 20 - 80;
        int addBtnY = panelY + panelH - 70;
        if (button == 0 && inside(mx, my, addBtnX, addBtnY, 80, 20)) {
            tryAdd();
            return true;
        }

        int listX = panelX + 20;
        int listW = panelW - 40;
        if (button == 0 && mx >= listX && mx <= listX + listW
                && my >= listTop && my <= listBottom) {
            IntrinsicConfig cfg = IntrinsicClient.getConfig();
            for (int i = 0; i < cfg.junkBlacklist.size(); i++) {
                int rowX = listX + 4;
                int rowY = listTop + i * (ROW_H + ROW_GAP) - scrollOffset;
                int rowW = listW - 8;
                int rmW = 60;
                int rmX = rowX + rowW - rmW - 3;
                int rmY = rowY + 2;
                if (inside(mx, my, rmX, rmY, rmW, ROW_H - 4)) {
                    cfg.junkBlacklist.remove(i);
                    cfg.save();
                    return true;
                }
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horiz, double vert) {
        int listX = panelX + 20;
        int listW = panelW - 40;
        if (mx >= listX && mx <= listX + listW && my >= listTop && my <= listBottom) {
            scrollOffset -= (int) (vert * (ROW_H + ROW_GAP));
            return true;
        }
        return super.mouseScrolled(mx, my, horiz, vert);
    }

    private void tryAdd() {
        String raw = addField.getValue().trim();
        if (raw.isEmpty()) { validationError = "Enter an item id."; return; }
        String id = JunkDropFeature.validateAndCanonicalize(raw);
        if (id == null) { validationError = "Unknown item: " + raw; return; }
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        if (cfg.junkBlacklist.contains(id)) {
            validationError = id + " is already in the list.";
            return;
        }
        cfg.junkBlacklist.add(id);
        cfg.save();
        addField.setValue("");
        validationError = null;
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }
}
