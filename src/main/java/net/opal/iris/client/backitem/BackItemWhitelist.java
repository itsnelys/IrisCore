package net.opal.iris.client.backitem;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.opal.iris.option.ConfigOptions;

import java.util.HashSet;
import java.util.Set;

/** Items that Iris may display on the local player's back. */
public final class BackItemWhitelist {
    private static final Set<Item> MANUAL_WHITELIST = new HashSet<>();

    private BackItemWhitelist() {}

    public static boolean isWhitelisted(Item item) {
        return item instanceof SwordItem || item instanceof PickaxeItem || item instanceof AxeItem
                || item instanceof ShovelItem || item instanceof HoeItem || item instanceof BowItem
                || item instanceof CrossbowItem || item instanceof ShieldItem || item instanceof TridentItem
                || item instanceof MaceItem || MANUAL_WHITELIST.contains(item) || isConfigured(item);
    }

    private static boolean isConfigured(Item item) {
        String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
        return ConfigOptions.getInstance().backItemWhitelist.stream()
                .map(String::trim)
                .anyMatch(itemId::equals);
    }

    public static void addManual(Item item) { MANUAL_WHITELIST.add(item); }
    public static void removeManual(Item item) { MANUAL_WHITELIST.remove(item); }
    public static void clearManual() { MANUAL_WHITELIST.clear(); }
    public static Set<Item> manualItems() { return Set.copyOf(MANUAL_WHITELIST); }
}
