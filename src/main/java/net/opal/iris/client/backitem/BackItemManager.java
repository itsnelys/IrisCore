package net.opal.iris.client.backitem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.opal.iris.commun.utils.FunctionUtilsChat;
import net.opal.iris.option.ConfigOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Maintains the local client-side item rendered on a player's back. */
public final class BackItemManager {
    private static final Map<UUID, ItemStack> BACK_ITEMS = new HashMap<>();
    private static ItemStack observedMainHand = ItemStack.EMPTY;

    private BackItemManager() {}

    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !ConfigOptions.getInstance().enableBackItems) {
            if (player != null && !BACK_ITEMS.isEmpty()) {
                FunctionUtilsChat.clientAction(player, "iris.back_items.disabled");
            }
            clear();
            observedMainHand = ItemStack.EMPTY;
            return;
        }

        ItemStack displayedBackItem = getBackItem(player.getUUID());
        if (!displayedBackItem.isEmpty() && !isStillInInventory(player, displayedBackItem)) {
            setBackItem(player.getUUID(), ItemStack.EMPTY);
            FunctionUtilsChat.clientAction(player, "iris.back_items.removed", displayedBackItem.getHoverName());
        }

        ItemStack currentMainHand = player.getMainHandItem();
        if (ItemStack.isSameItemSameComponents(currentMainHand, observedMainHand)) return;

        // Keep the item that was just put away. The active item is never drawn
        // on the back by the render layer, so switching tools exposes the last one.
        if (!observedMainHand.isEmpty() && BackItemWhitelist.isWhitelisted(observedMainHand.getItem())) {
            setBackItem(player.getUUID(), observedMainHand);
            FunctionUtilsChat.clientAction(player, "iris.back_items.tracked", observedMainHand.getHoverName());
        }
        observedMainHand = currentMainHand.copy();
    }

    public static void setBackItem(UUID playerId, ItemStack stack) {
        if (stack == null || stack.isEmpty()) BACK_ITEMS.remove(playerId);
        else BACK_ITEMS.put(playerId, stack.copy());
    }

    public static ItemStack getBackItem(UUID playerId) {
        return BACK_ITEMS.getOrDefault(playerId, ItemStack.EMPTY);
    }

    public static void clear() {
        BACK_ITEMS.clear();
    }

    private static boolean isStillInInventory(LocalPlayer player, ItemStack expected) {
        for (ItemStack candidate : player.getInventory().items) {
            if (ItemStack.isSameItemSameComponents(candidate, expected)) return true;
        }
        return ItemStack.isSameItemSameComponents(player.getOffhandItem(), expected);
    }

}
