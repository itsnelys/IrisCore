package net.opal.iris.client.backitem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.opal.iris.commun.utils.FunctionUtilsChat;
import net.opal.iris.network.BackItemLockPayload;
import net.opal.iris.option.ConfigOptions;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Maintains the local client-side item rendered on a player's back. */
public final class BackItemManager {
    private static final Map<UUID, ItemStack> BACK_ITEMS = new HashMap<>();
    private static final Map<UUID, ItemStack> LOCKED_BACK_ITEMS = new HashMap<>();
    private static ItemStack observedMainHand = ItemStack.EMPTY;

    private BackItemManager() {}

    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !ConfigOptions.getInstance().enableBackItems) {
            if (player != null && !BACK_ITEMS.isEmpty()) {
                if (isLocked(player.getUUID())) {
                    PacketDistributor.sendToServer(new BackItemLockPayload(ItemStack.EMPTY));
                }
                FunctionUtilsChat.clientAction(player, "iris.back_items.disabled");
            }
            clear();
            observedMainHand = ItemStack.EMPTY;
            return;
        }

        ItemStack displayedBackItem = getBackItem(player.getUUID());
        if (!displayedBackItem.isEmpty() && !isStillInInventory(player, displayedBackItem)) {
            setBackItem(player.getUUID(), ItemStack.EMPTY);
            ItemStack lockedBackItem = LOCKED_BACK_ITEMS.get(player.getUUID());
            if (sameStack(displayedBackItem, lockedBackItem)) {
                LOCKED_BACK_ITEMS.remove(player.getUUID());
                PacketDistributor.sendToServer(new BackItemLockPayload(ItemStack.EMPTY));
                FunctionUtilsChat.clientAction(player, "iris.back_items.lock_removed", displayedBackItem.getHoverName());
            }
            FunctionUtilsChat.clientAction(player, "iris.back_items.removed", displayedBackItem.getHoverName());
        }

        ItemStack currentMainHand = player.getMainHandItem();
        if (sameStack(currentMainHand, observedMainHand)) return;

        // Keep the item that was just put away. The active item is never drawn
        // on the back by the render layer, so switching tools exposes the last one.
        if (!isLocked(player.getUUID()) && !observedMainHand.isEmpty()
                && BackItemWhitelist.isWhitelisted(observedMainHand.getItem())) {
            setBackItem(player.getUUID(), observedMainHand);
            FunctionUtilsChat.clientAction(player, "iris.back_items.tracked", observedMainHand.getHoverName());
        }
        observedMainHand = currentMainHand.copy();
    }

    /** Locks or unlocks the held eligible item as the local player's preferred back item. */
    public static void toggleLockedMainHand() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !ConfigOptions.getInstance().enableBackItems) return;

        UUID playerId = player.getUUID();
        ItemStack heldItem = player.getMainHandItem();
        ItemStack lockedItem = LOCKED_BACK_ITEMS.getOrDefault(playerId, ItemStack.EMPTY);
        if (!lockedItem.isEmpty() && sameStack(lockedItem, heldItem)) {
            LOCKED_BACK_ITEMS.remove(playerId);
            setBackItem(playerId, ItemStack.EMPTY);
            PacketDistributor.sendToServer(new BackItemLockPayload(ItemStack.EMPTY));
            FunctionUtilsChat.clientAction(player, "iris.back_items.unlocked", heldItem.getHoverName());
            return;
        }

        if (heldItem.isEmpty() || !BackItemWhitelist.isWhitelisted(heldItem.getItem())) {
            FunctionUtilsChat.clientAction(player, "iris.back_items.lock_invalid");
            return;
        }

        ItemStack lock = heldItem.copy();
        LOCKED_BACK_ITEMS.put(playerId, lock);
        setBackItem(playerId, lock);
        observedMainHand = lock.copy();
        PacketDistributor.sendToServer(new BackItemLockPayload(lock));
        FunctionUtilsChat.clientAction(player, "iris.back_items.locked", lock.getHoverName());
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
        LOCKED_BACK_ITEMS.clear();
    }

    /** Removes the visual lock after the server blocks an attack with the back shield. */
    public static void detachLockedBackItemAfterBlock() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack lockedItem = LOCKED_BACK_ITEMS.remove(player.getUUID());
        if (lockedItem == null || lockedItem.isEmpty()) return;
        setBackItem(player.getUUID(), ItemStack.EMPTY);
        FunctionUtilsChat.clientAction(player, "iris.back_items.shield_detached", lockedItem.getHoverName());
    }

    private static boolean isLocked(UUID playerId) {
        return LOCKED_BACK_ITEMS.containsKey(playerId);
    }

    private static boolean isStillInInventory(LocalPlayer player, ItemStack expected) {
        for (ItemStack candidate : player.getInventory().items) {
            if (sameStack(candidate, expected)) return true;
        }
        return sameStack(player.getMainHandItem(), expected) || sameStack(player.getOffhandItem(), expected);
    }

    private static boolean sameStack(ItemStack first, ItemStack second) {
        return first != null && second != null && ItemStack.isSameItemSameComponents(first, second);
    }

}
