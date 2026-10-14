package net.opal.iris.menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Shared options hub for Iris modules. A module registers one entry and owns
 * the screen opened by that entry, so IrisCore stays independent of modules.
 */
public final class IrisModuleOptionsRegistry {
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private IrisModuleOptionsRegistry() {}

    public static synchronized void register(String id, Component title, Function<Screen, Screen> screenFactory) {
        ENTRIES.removeIf(entry -> entry.id().equals(id));
        ENTRIES.add(new Entry(id, title, screenFactory));
        ENTRIES.sort(Comparator.comparing(Entry::id));
    }

    public static synchronized List<Entry> entries() {
        return List.copyOf(ENTRIES);
    }

    public record Entry(String id, Component title, Function<Screen, Screen> screenFactory) {}
}
