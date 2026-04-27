package com.intrinsic.client.mixin;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.util.ShulkerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin {
    @Shadow
    protected Slot hoveredSlot;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void intrinsic_renderShulkerPreview(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta,
                                                   CallbackInfo ci) {
        if (!IntrinsicClient.getConfig().shulkerPreview) return;
        if (hoveredSlot == null) return;

        ItemStack hovered = hoveredSlot.getItem();
        if (!ShulkerUtil.isShulkerBox(hovered)) return;

        List<ItemStack> all = ShulkerUtil.getContents(hovered);
        List<ItemStack> nonEmpty = new ArrayList<>(all.size());
        for (ItemStack s : all) if (s != null && !s.isEmpty()) nonEmpty.add(s);
        if (nonEmpty.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        int screenW = screen.width;
        int screenH = screen.height;

        int slotSize = 18;
        int pad = 4;

        int cols = Math.min(9, nonEmpty.size());
        int rows = (nonEmpty.size() + cols - 1) / cols;
        int gridW = cols * slotSize;
        int gridH = rows * slotSize;
        int panelW = gridW + pad * 2;
        int panelH = gridH + pad * 2;

        // Vanilla item tooltip is drawn at (mouseX + 12, mouseY - 12) and is ~18px tall.
        // Place the preview panel directly above it so the shulker's name stays visible
        // below the grid instead of being overlapped.
        int tooltipTop = mouseY - 12;
        int gap = 4;
        int px = mouseX + 12;
        int py = tooltipTop - gap - panelH;
        if (px + panelW > screenW) px = mouseX - panelW - 12;
        if (py < 4) py = mouseY + 20;
        if (py + panelH > screenH) py = screenH - panelH - 4;
        if (px < 4) px = 4;

        drawPanel(ctx, px, py, panelW, panelH);

        int gridX = px + pad;
        int gridY = py + pad;
        for (int i = 0; i < nonEmpty.size(); i++) {
            ItemStack stack = nonEmpty.get(i);
            int sx = gridX + (i % cols) * slotSize + 1;
            int sy = gridY + (i / cols) * slotSize + 1;
            ctx.item(stack, sx, sy);
            ctx.itemDecorations(mc.font, stack, sx, sy);
        }
    }

    private static void drawPanel(GuiGraphicsExtractor ctx, int px, int py, int w, int h) {
        int bg = 0xF0100010;
        int border1 = 0x505000FF;
        int border2 = 0x5028007F;
        ctx.fill(px, py, px + w, py + h, bg);
        ctx.fill(px, py - 1, px + w, py, bg);
        ctx.fill(px, py + h, px + w, py + h + 1, bg);
        ctx.fill(px - 1, py, px, py + h, bg);
        ctx.fill(px + w, py, px + w + 1, py + h, bg);
        ctx.fillGradient(px, py + 1, px + 1, py + h - 1, border1, border2);
        ctx.fillGradient(px + w - 1, py + 1, px + w, py + h - 1, border1, border2);
        ctx.fillGradient(px + 1, py, px + w - 1, py + 1, border1, border1);
        ctx.fillGradient(px + 1, py + h - 1, px + w - 1, py + h, border2, border2);
    }
}
