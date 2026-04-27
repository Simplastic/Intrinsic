package com.intrinsic.client.mixin;

import com.intrinsic.client.feature.ContainerContentCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenCaptureMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private void intrinsic_capturePendingPos(CallbackInfo ci) {
        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
        AbstractContainerMenu menu = self.getMenu();
        if (!isCacheable(menu)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            ContainerContentCache.clearPending();
            return;
        }
        HitResult hr = mc.hitResult;
        if (!(hr instanceof BlockHitResult bhr) || bhr.getType() == HitResult.Type.MISS) {
            ContainerContentCache.clearPending();
            return;
        }
        BlockPos pos = bhr.getBlockPos();
        String title = self.getTitle() != null ? self.getTitle().getString() : "";
        ContainerContentCache.markPending(mc.level, pos, title);
    }

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void intrinsic_captureSlots(CallbackInfo ci) {
        if (!ContainerContentCache.hasPending()) return;
        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
        AbstractContainerMenu menu = self.getMenu();
        if (!isCacheable(menu)) return;

        int total = menu.slots.size();
        int containerSize = total - 36;
        if (containerSize <= 0) return;
        List<ItemStack> snap = new ArrayList<>(containerSize);
        for (int i = 0; i < containerSize; i++) {
            snap.add(menu.getSlot(i).getItem().copy());
        }
        ContainerContentCache.recordPending(snap);
    }

    private static boolean isCacheable(AbstractContainerMenu menu) {
        if (menu == null) return false;
        if (menu instanceof InventoryMenu) return false;
        if (menu instanceof MerchantMenu) return false;
        if (menu instanceof CreativeModeInventoryScreen.ItemPickerMenu) return false;
        if (menu instanceof AbstractMountInventoryMenu) return false;
        return menu.slots.size() > 36;
    }
}
