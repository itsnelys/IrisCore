package net.opal.iris.menu;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.opal.iris.Iris;

import java.util.Optional;

public class MainMenu {
    private static final int BUTTON_HEIGHT = 20;
    private static final int DEFAULT_BUTTON_WIDTH = 200;
    private static final int ROW_GAP = 4;

    @SubscribeEvent
    public static void onInitScreen(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();

        if (screen instanceof TitleScreen) {
            Anchor modsAnchor = findModsAnchor(screen)
                    .orElseGet(() -> new Anchor((screen.width - DEFAULT_BUTTON_WIDTH) / 2, screen.height / 4 + 96, DEFAULT_BUTTON_WIDTH));
            moveButtonsBelowMods(screen, modsAnchor);
            int irisY = modsAnchor.y() + BUTTON_HEIGHT + ROW_GAP;
            Button irisOptionsButton = Button.builder(
                            Component.translatable("menu.iris.options"),
                            b -> screen.getMinecraft().setScreen(new MenuOptionIris(screen)))
                    .pos(modsAnchor.x(), irisY)
                    .size(modsAnchor.width(), BUTTON_HEIGHT)
                    .build();
            event.addListener(irisOptionsButton);
        }
    }

    private static Optional<Anchor> findModsAnchor(Screen screen) {
        return screen.children().stream()
                .filter(AbstractWidget.class::isInstance)
                .map(AbstractWidget.class::cast)
                .filter(widget -> widget instanceof Button)
                .filter(widget -> "Mods".equals(widget.getMessage().getString()))
                .findFirst()
                .map(widget -> new Anchor(widget.getX(), widget.getY(), widget.getWidth()));
    }

    private static void moveButtonsBelowMods(Screen screen, Anchor modsAnchor) {
        screen.children().stream()
                .filter(AbstractWidget.class::isInstance)
                .map(AbstractWidget.class::cast)
                .filter(widget -> widget instanceof Button)
                .filter(widget -> widget.getY() > modsAnchor.y())
                .forEach(widget -> widget.setY(widget.getY() + BUTTON_HEIGHT + ROW_GAP));
    }

    private record Anchor(int x, int y, int width) {}
}
