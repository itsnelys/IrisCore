package net.opal.iris.indicators.providers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.opal.iris.api.IIndicator;
import net.opal.iris.client.backitem.BackItemManager;
import net.opal.iris.option.ConfigOptions;

/** Displays the item explicitly attached to the player's back with Shift+B. */
public final class BackItemIndicator implements IIndicator {
    @Override
    public boolean isVisible() {
        var player = Minecraft.getInstance().player;
        return player != null
                && !player.isSpectator()
                && ConfigOptions.getInstance().enableIndicators
                && ConfigOptions.getInstance().enableBackItems
                && BackItemManager.isBackItemLocked(player.getUUID())
                && !BackItemManager.getBackItem(player.getUUID()).isEmpty();
    }

    @Override
    public void render(GuiGraphics gui, int x, int y, float partialTick) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null) return;

        ItemStack stack = BackItemManager.getBackItem(player.getUUID());
        if (stack.isEmpty()) return;

        gui.renderItem(stack, x, y);
        gui.renderItemDecorations(minecraft.font, stack, x, y);
        // A small green L distinguishes the item locked with Shift+B from the other HUD items.
        gui.drawString(minecraft.font, "L", x + 1, y + 1, 0xFF55FF55, true);
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
