package com.intrinsic.client.gui;

import com.intrinsic.client.audio.AudioRegistry;
import com.intrinsic.client.config.IntrinsicConfig;
import com.intrinsic.client.gui.widget.ThemeColors;
import com.intrinsic.client.gui.widget.VolumeSlider;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.lwjgl.glfw.GLFW;

public class CustomAudioEditorScreen extends Screen {
    private final Screen parent;
    private final IntrinsicConfig.CustomAudioPattern editing;

    private EditBox nameField;
    private EditBox patternField;
    private VolumeSlider volumeSlider;
    private String validationError = null;

    private int panelX, panelY, panelW, panelH;

    public CustomAudioEditorScreen(Screen parent, IntrinsicConfig.CustomAudioPattern editing) {
        super(Component.literal(editing == null ? "Add Audio Pattern" : "Edit Audio Pattern"));
        this.parent = parent;
        this.editing = editing;
    }

    @Override
    protected void init() {
        panelW = 420;
        panelH = 240;
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        int fieldW = panelW - 40;

        nameField = new EditBox(this.font, panelX + 20, panelY + 56, fieldW, 20, Component.literal("Display name"));
        nameField.setMaxLength(48);
        nameField.setHint(Component.literal("Display name (e.g. Chests)").withStyle(ChatFormatting.DARK_GRAY));
        if (editing != null && editing.displayName != null) nameField.setValue(editing.displayName);
        this.addRenderableWidget(nameField);

        patternField = new EditBox(this.font, panelX + 20, panelY + 108, fieldW, 20, Component.literal("Sound id prefix"));
        patternField.setMaxLength(96);
        patternField.setHint(Component.literal("Sound id prefix (e.g. block.chest.)").withStyle(ChatFormatting.DARK_GRAY));
        if (editing != null && editing.pattern != null) patternField.setValue(editing.pattern);
        this.addRenderableWidget(patternField);

        double startVolume = editing != null ? editing.volume : 1.0;
        volumeSlider = new VolumeSlider(panelX + 20, panelY + 154, fieldW, 20, startVolume, 0.0, 2.0);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        super.extractRenderState(ctx, mouseX, mouseY, delta);
        ctx.fill(0, 0, this.width, this.height, ThemeColors.BACKDROP);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, ThemeColors.PANEL);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 1, ThemeColors.ACCENT);

        ctx.text(this.font, Component.literal(editing == null ? "New audio pattern" : "Edit audio pattern").withStyle(ChatFormatting.BOLD), panelX + 20, panelY + 14, ThemeColors.TEXT, true);

        ctx.text(this.font, Component.literal("Display name").withStyle(ChatFormatting.GRAY), panelX + 20, panelY + 44, ThemeColors.TEXT_DIM, true);
        nameField.extractRenderState(ctx, mouseX, mouseY, delta);

        ctx.text(this.font, Component.literal("Sound id prefix").withStyle(ChatFormatting.GRAY), panelX + 20, panelY + 96, ThemeColors.TEXT_DIM, true);
        patternField.extractRenderState(ctx, mouseX, mouseY, delta);

        ctx.text(this.font, Component.literal("Volume").withStyle(ChatFormatting.GRAY), panelX + 20, panelY + 142, ThemeColors.TEXT_DIM, true);
        volumeSlider.render(ctx, this.font, mouseX, mouseY, true);

        int btnY = panelY + panelH - 36;
        int btnW = 120, btnH = 22;
        int cancelX = panelX + panelW - 20 - btnW - 12 - btnW;
        int saveX = panelX + panelW - 20 - btnW;

        drawButton(ctx, "Cancel", cancelX, btnY, btnW, btnH, mouseX, mouseY, false);
        drawButton(ctx, editing == null ? "Add" : "Save", saveX, btnY, btnW, btnH, mouseX, mouseY, true);

        if (validationError != null) {
            ctx.text(this.font, Component.literal(validationError).withStyle(ChatFormatting.RED), panelX + 20, btnY + 7, 0xFFFF6666, true);
        }

        super.extractRenderState(ctx, mouseX, mouseY, delta);
    }

    private void drawButton(GuiGraphicsExtractor ctx, String label, int x, int y, int w, int h,
                             int mouseX, int mouseY, boolean primary) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = primary
                ? (hover ? ThemeColors.ACCENT_HOVER : ThemeColors.ACCENT)
                : (hover ? ThemeColors.CARD_HOVER : ThemeColors.CHIP);
        ctx.fill(x, y, x + w, y + h, bg);
        int tw = this.font.width(label);
        ctx.text(this.font, Component.literal(label), x + (w - tw) / 2, y + (h - 8) / 2, ThemeColors.TEXT, true);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (volumeSlider.mouseClicked(click.x(), click.y(), click.button())) return true;

        int btnY = panelY + panelH - 36;
        int btnW = 120, btnH = 22;
        int cancelX = panelX + panelW - 20 - btnW - 12 - btnW;
        int saveX = panelX + panelW - 20 - btnW;
        if (inside(click.x(), click.y(), cancelX, btnY, btnW, btnH)) {
            onClose();
            return true;
        }
        if (inside(click.x(), click.y(), saveX, btnY, btnW, btnH)) {
            if (trySave()) onClose();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        if (volumeSlider.mouseDragged(click.x(), click.y(), click.button())) return true;
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (volumeSlider.mouseReleased(click.x(), click.y(), click.button())) return true;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horiz, double vert) {
        if (volumeSlider.mouseScrolled(mx, my, vert, isShiftDown())) return true;
        return super.mouseScrolled(mx, my, horiz, vert);
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private boolean isShiftDown() {
        if (this.minecraft == null) return false;
        long w = this.minecraft.getWindow().handle();
        return GLFW.glfwGetKey(w, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(w, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    private boolean trySave() {
        String name = nameField.getValue().trim();
        String pattern = patternField.getValue().trim();
        if (name.isEmpty()) { validationError = "Display name is required."; return false; }
        if (pattern.isEmpty()) { validationError = "Sound id prefix is required."; return false; }
        if (!pattern.matches("[a-z0-9_./]+")) {
            validationError = "Prefix must be lowercase letters, digits, dot, or underscore.";
            return false;
        }
        double volume = Math.max(0.0, Math.min(2.0, volumeSlider.value));
        if (editing == null) {
            AudioRegistry.addCustomPattern(name, pattern, volume);
        } else {
            AudioRegistry.updateCustomPattern(editing.id, name, pattern, volume);
        }
        validationError = null;
        return true;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }
}
