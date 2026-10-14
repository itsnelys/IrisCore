package net.opal.iris.indicators.providers;

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

        ToolDurabilityIndicator.renderDurability(gui, minecraft, stack, x, y, partialTick);
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public int getWidth() {
        return 16;
    }

}
