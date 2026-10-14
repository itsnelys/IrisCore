package net.opal.iris.indicators.providers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.opal.iris.api.IIndicator;
import net.opal.iris.client.backitem.BackItemManager;
import net.opal.iris.option.ConfigOptions;

/** Displays the current eligible BackItem and its durability in the HUD. */
public final class BackItemIndicator implements IIndicator {
    @Override
    public boolean isVisible() {
        var player = Minecraft.getInstance().player;
        if (player == null
                || player.isSpectator()
                || !ConfigOptions.getInstance().enableIndicators
                || !ConfigOptions.getInstance().enableBackItems) return false;

        ItemStack stack = BackItemManager.getLiveBackItem(player);
        if (stack.isEmpty()) return false;

        // The same stack cannot be displayed as both a held-item indicator and
        // a BackItem indicator. This applies to manual Shift+B locks and the
        // automatically tracked last-held item alike.
        return !ItemStack.isSameItemSameComponents(player.getMainHandItem(), stack)
                && !ItemStack.isSameItemSameComponents(player.getOffhandItem(), stack);
    }

    @Override
    public void render(GuiGraphics gui, int x, int y, float partialTick) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null) return;

        ItemStack stack = BackItemManager.getLiveBackItem(player);
        if (stack.isEmpty()) return;

        float durabilityRatio = getDurabilityRatio(stack);
        if (durabilityRatio <= 0.10F && !isLowDurabilityVisible(player.tickCount, durabilityRatio)) return;

        // ItemRenderer does not consistently honour alpha on every item model.
        // Alternating the complete indicator guarantees the same warning is visible
        // for shields and every other damageable BackItem.
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        gui.renderItem(stack, x, y);
        gui.renderItemDecorations(minecraft.font, stack, x, y);
        gui.flush();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (!ConfigOptions.getInstance().showDurabilityDetails || !stack.isDamageableItem()) return;

        int remaining = stack.getMaxDamage() - stack.getDamageValue();
        String text = String.valueOf(remaining);
        float scale = remaining > 999 ? 0.55F : 0.65F;
        float textWidth = minecraft.font.width(text) * scale;
        gui.pose().pushPose();
        gui.pose().translate(x + 16 - textWidth, y + 9, 200);
        gui.pose().scale(scale, scale, 1.0F);
        gui.drawString(minecraft.font, text, 0, 0, 0xFFFFFFFF, true);
        gui.pose().popPose();
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public int getWidth() {
        return 16;
    }

    private static float getDurabilityRatio(ItemStack stack) {
        if (!stack.isDamageableItem() || stack.getMaxDamage() <= 0) return 1.0F;
        return (stack.getMaxDamage() - stack.getDamageValue()) / (float) stack.getMaxDamage();
    }

    private static boolean isLowDurabilityVisible(int tickCount, float durabilityRatio) {
        int halfCycleTicks = durabilityRatio <= 0.05F ? 3 : 6;
        return (tickCount / halfCycleTicks) % 2 == 0;
    }
}
