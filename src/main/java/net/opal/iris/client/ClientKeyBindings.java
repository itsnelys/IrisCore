package net.opal.iris.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.opal.iris.client.backitem.BackItemManager;
import net.opal.iris.option.ConfigOptions;
import org.lwjgl.glfw.GLFW;

/** Key mappings belong to Iris; feature modules attach their actions through callbacks. */
public final class ClientKeyBindings {
    private static final String CATEGORY = "key.categories.iris";
    private static final KeyMapping TOGGLE_OVERLAY = new KeyMapping("key.iris.toggle_overlay", GLFW.GLFW_KEY_V, CATEGORY);
    private static final KeyMapping LOCK_BACK_ITEM = new KeyMapping("key.iris.lock_back_item", GLFW.GLFW_KEY_B, CATEGORY);
    public static final KeyMapping RECIPES = new KeyMapping("key.iris.recipes", GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
    public static final KeyMapping USES = new KeyMapping("key.iris.uses", GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
    public static final KeyMapping PIN = new KeyMapping("key.iris.pin", GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
    private static final KeyMapping PIN_VISIBILITY = new KeyMapping("key.iris.pin_visibility", GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
    private static Runnable pinVisibilityHandler = () -> {};

    private ClientKeyBindings() {}

    public static void setPinVisibilityHandler(Runnable handler) {
        pinVisibilityHandler = handler == null ? () -> {} : handler;
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_OVERLAY);
        event.register(LOCK_BACK_ITEM);
        event.register(RECIPES);
        event.register(USES);
        event.register(PIN);
        event.register(PIN_VISIBILITY);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (LOCK_BACK_ITEM.consumeClick()) {
            if (Minecraft.getInstance().screen == null && Screen.hasShiftDown()) {
                BackItemManager.toggleLockedMainHand();
            }
        }
        while (PIN_VISIBILITY.consumeClick()) {
            if (Minecraft.getInstance().screen == null) pinVisibilityHandler.run();
        }
        while (TOGGLE_OVERLAY.consumeClick()) {
            if (Minecraft.getInstance().screen != null || Minecraft.getInstance().player == null) continue;
            ConfigOptions config = ConfigOptions.getInstance();
            config.enableBlockTooltipOverlay = !config.enableBlockTooltipOverlay;
            config.save();
            Minecraft.getInstance().player.displayClientMessage(Component.translatable(
                    "key.iris.toggle_overlay.message",
                    Component.translatable(config.enableBlockTooltipOverlay ? "menu.iris.on" : "menu.iris.off")), true);
        }
    }
}
