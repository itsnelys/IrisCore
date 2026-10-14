package net.opal.iris.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.opal.iris.Iris;
import net.opal.iris.client.backitem.BackItemManager;
import net.opal.iris.client.backitem.BackItemRenderPlayer;
import net.opal.iris.indicators.IndicatorOverlayRenderer;
import net.opal.iris.menu.MainMenu;
import net.opal.iris.menu.MainMenuTitleOverlay;
import net.opal.iris.menu.MenuPause;

/** Registers the Iris-only HUD and options-menu features. */
public final class IrisClientEventRegistration {
    private IrisClientEventRegistration() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(IrisClientEventRegistration::registerGuiLayers);
        modEventBus.addListener(BackItemRenderPlayer::onAddLayers);
        modEventBus.addListener(ClientKeyBindings::register);
        NeoForge.EVENT_BUS.addListener(ClientKeyBindings::onClientTick);
        NeoForge.EVENT_BUS.addListener(BackItemManager::onClientTick);
        NeoForge.EVENT_BUS.register(MenuPause.class);
        NeoForge.EVENT_BUS.register(MainMenu.class);
        NeoForge.EVENT_BUS.register(MainMenuTitleOverlay.class);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(Iris.MODID, "indicators"),
                IndicatorOverlayRenderer::render);
    }
}
