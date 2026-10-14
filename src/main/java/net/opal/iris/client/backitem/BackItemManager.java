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
    private static final Map<UUID, Integer> BACK_ITEM_SLOTS = new HashMap<>();
    private static ItemStack observedMainHand = ItemStack.EMPTY;
    private static int observedSelectedSlot = -1;

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
            observedSelectedSlot = -1;
            return;
        }

        ItemStack displayedBackItem = getBackItem(player.getUUID());
        if (!displayedBackItem.isEmpty() && !isStillInInventory(player, displayedBackItem)) {
            setBackItem(player.getUUID(), ItemStack.EMPTY);
            ItemStack lockedBackItem = LOCKED_BACK_ITEMS.get(player.getUUID());
            if (sameTrackedItem(displayedBackItem, lockedBackItem)) {
                LOCKED_BACK_ITEMS.remove(player.getUUID());
                FunctionUtilsChat.clientAction(player, "iris.back_items.lock_removed", displayedBackItem.getHoverName());
            }
            FunctionUtilsChat.clientAction(player, "iris.back_items.removed", displayedBackItem.getHoverName());
        }

        ItemStack currentMainHand = player.getMainHandItem();
        if (sameTrackedItem(currentMainHand, observedMainHand)) return;

        // Keep the item that was just put away. The active item is never drawn
        // on the back by the render layer, so switching tools exposes the last one.
        if (!isLocked(player.getUUID()) && !observedMainHand.isEmpty()
                && BackItemWhitelist.isWhitelisted(observedMainHand.getItem())) {
            setBackItem(player.getUUID(), observedMainHand);
            BACK_ITEM_SLOTS.put(player.getUUID(), observedSelectedSlot);
            FunctionUtilsChat.clientAction(player, "iris.back_items.tracked", observedMainHand.getHoverName());
        }
        observedMainHand = currentMainHand.copy();
        observedSelectedSlot = player.getInventory().selected;
    }

    /** Locks or unlocks the held eligible item as the local player's preferred back item. */
    public static void toggleLockedMainHand() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !ConfigOptions.getInstance().enableBackItems) return;

        UUID playerId = player.getUUID();
        ItemStack heldItem = player.getMainHandItem();
        ItemStack lockedItem = LOCKED_BACK_ITEMS.getOrDefault(playerId, ItemStack.EMPTY);
        if (!lockedItem.isEmpty() && sameTrackedItem(lockedItem, heldItem)) {
            LOCKED_BACK_ITEMS.remove(playerId);
            setBackItem(playerId, ItemStack.EMPTY);
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
        BACK_ITEM_SLOTS.put(playerId, player.getInventory().selected);
        observedMainHand = lock.copy();
        observedSelectedSlot = player.getInventory().selected;
        FunctionUtilsChat.clientAction(player, "iris.back_items.locked", lock.getHoverName());
    }

    public static void setBackItem(UUID playerId, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            BACK_ITEMS.remove(playerId);
            BACK_ITEM_SLOTS.remove(playerId);
        } else BACK_ITEMS.put(playerId, stack.copy());

        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null && localPlayer.getUUID().equals(playerId)) {
            PacketDistributor.sendToServer(new BackItemLockPayload(stack == null ? ItemStack.EMPTY : stack));
        }
    }

    public static ItemStack getBackItem(UUID playerId) {
        return BACK_ITEMS.getOrDefault(playerId, ItemStack.EMPTY);
    }

    /**
     * Resolves the tracked item to its live inventory stack. A BackItem is stored
     * as a copy for rendering, while durability damage changes the real stack.
     */
    public static ItemStack getLiveBackItem(LocalPlayer player) {
        if (player == null) return ItemStack.EMPTY;

        ItemStack trackedItem = getBackItem(player.getUUID());
        if (trackedItem.isEmpty()) return ItemStack.EMPTY;

        int sourceSlot = BACK_ITEM_SLOTS.getOrDefault(player.getUUID(), -1);
        if (sourceSlot >= 0 && sourceSlot < player.getInventory().items.size()) {
            ItemStack sourceStack = player.getInventory().getItem(sourceSlot);
            if (sameTrackedItem(sourceStack, trackedItem)) return sourceStack;
        }
        return trackedItem;
    }

    /** Returns whether the player's displayed back item was explicitly selected with Shift+B. */
    public static boolean isBackItemLocked(UUID playerId) {
        return isLocked(playerId);
    }

    public static void clear() {
        BACK_ITEMS.clear();
        LOCKED_BACK_ITEMS.clear();
        BACK_ITEM_SLOTS.clear();
    }

    /** Removes the visual lock after the server blocks an attack with the back shield. */
    public static void detachLockedBackItemAfterBlock() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack lockedItem = LOCKED_BACK_ITEMS.remove(player.getUUID());
        ItemStack displayedItem = getBackItem(player.getUUID());
        ItemStack detachedItem = lockedItem == null || lockedItem.isEmpty() ? displayedItem : lockedItem;
        if (detachedItem.isEmpty()) return;
        setBackItem(player.getUUID(), ItemStack.EMPTY);
        FunctionUtilsChat.clientAction(player, "iris.back_items.shield_detached", detachedItem.getHoverName());
    }

    private static boolean isLocked(UUID playerId) {
        return LOCKED_BACK_ITEMS.containsKey(playerId);
    }

    private static boolean isStillInInventory(LocalPlayer player, ItemStack expected) {
        for (ItemStack candidate : player.getInventory().items) {
            if (sameTrackedItem(candidate, expected)) return true;
        }
        return sameTrackedItem(player.getMainHandItem(), expected)
                || sameTrackedItem(player.getOffhandItem(), expected);
    }

    /**
     * A tool's durability changes while it is used. BackItems therefore tracks
     * its item identity, rather than treating a changed damage component as a
     * different item that should be detached.
     */
    private static boolean sameTrackedItem(ItemStack first, ItemStack second) {
        return first != null && second != null && !first.isEmpty() && !second.isEmpty()
                && ItemStack.isSameItem(first, second);
    }

}
