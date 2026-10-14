package net.opal.iris.indicators.providers;

import com.mojang.blaze3d.systems.RenderSystem;
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

        ItemStack stack = BackItemManager.getLiveBackItem(player);
        if (stack.isEmpty()) return;

        renderDurabilityState(minecraft, stack, partialTick);
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

    private static void renderDurabilityState(Minecraft minecraft, ItemStack stack, float partialTick) {
        if (!stack.isDamageableItem() || stack.getMaxDamage() <= 0 || minecraft.player == null) return;

        float ratio = (stack.getMaxDamage() - stack.getDamageValue()) / (float) stack.getMaxDamage();
        if (ratio > 0.10F) return;

        float speed = ratio <= 0.05F ? 1.2F : 0.6F;
        float time = (minecraft.player.tickCount + partialTick) * speed;
        float alpha = 0.5F + (float) Math.sin(time) * 0.35F;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
    }
}
