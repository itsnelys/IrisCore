package net.opal.iris.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.opal.iris.option.ConfigOptions;
import net.opal.iris.theme.UiTheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/** Dedicated, in-game configuration page for visual BackItems settings. */
public final class BackItemsOptionsScreen extends Screen {
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 4;

    private final Screen parent;
    private EditBox whitelistField;

    public BackItemsOptionsScreen(Screen parent) {
        super(Component.translatable("menu.iris.back_items.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ConfigOptions config = ConfigOptions.getInstance();
        int panelWidth = Math.min(320, Math.max(180, width - 36));
        int left = (width - panelWidth) / 2;
        int half = (panelWidth - GAP) / 2;

        addRenderableWidget(toggleButton(left, 50, panelWidth, "menu.iris.back_items", config.enableBackItems,
                enabled -> config.enableBackItems = enabled));

        addRenderableWidget(toggleButton(left, 105, half, "menu.iris.back_items.first_person", config.hideBackItemsInFirstPerson,
                enabled -> config.hideBackItemsInFirstPerson = enabled));
        addRenderableWidget(toggleButton(left + half + GAP, 105, half, "menu.iris.back_items.swimming", config.hideBackItemsWhileSwimming,
                enabled -> config.hideBackItemsWhileSwimming = enabled));
        addRenderableWidget(toggleButton(left, 130, half, "menu.iris.back_items.elytra", config.hideBackItemsWhileFallFlying,
                enabled -> config.hideBackItemsWhileFallFlying = enabled));
        addRenderableWidget(toggleButton(left + half + GAP, 130, half, "menu.iris.back_items.invisible", config.hideBackItemsWhenInvisible,
                enabled -> config.hideBackItemsWhenInvisible = enabled));
        addRenderableWidget(toggleButton(left, 155, half, "menu.iris.back_items.crouching", config.hideBackItemsWhileCrouching,
                enabled -> config.hideBackItemsWhileCrouching = enabled));
        addRenderableWidget(toggleButton(left + half + GAP, 155, half, "menu.iris.back_items.riding", config.hideBackItemsWhileRiding,
                enabled -> config.hideBackItemsWhileRiding = enabled));
        addRenderableWidget(toggleButton(left, 180, half, "menu.iris.back_items.spectator", config.hideBackItemsInSpectator,
                enabled -> config.hideBackItemsInSpectator = enabled));
        addRenderableWidget(toggleButton(left + half + GAP, 180, half, "menu.iris.back_items.sleeping", config.hideBackItemsWhileSleeping,
                enabled -> config.hideBackItemsWhileSleeping = enabled));
        addRenderableWidget(toggleButton(left, 205, panelWidth, "menu.iris.back_items.cape", config.hideBackItemsWithCape,
                enabled -> config.hideBackItemsWithCape = enabled));

        whitelistField = new EditBox(font, left, 260, panelWidth, BUTTON_HEIGHT,
                Component.translatable("menu.iris.back_items.whitelist"));
        whitelistField.setMaxLength(1024);
        whitelistField.setValue(String.join(", ", config.backItemWhitelist));
        whitelistField.setHint(Component.translatable("menu.iris.back_items.whitelist_hint"));
        addRenderableWidget(whitelistField);

        addRenderableWidget(Button.builder(Component.translatable("menu.iris.back_items.save_whitelist"), button -> saveWhitelist())
                .bounds(left, 285, panelWidth, BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("menu.iris.return"), button -> onClose())
                .bounds(left, height - 30, panelWidth, BUTTON_HEIGHT).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        UiTheme theme = UiTheme.getCurrent();
        graphics.fill(0, 0, width, height, theme.gui_bgOverlay());
        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics.fill(0, 0, width, 35, theme.gui_barColor());
        graphics.fill(0, 34, width, 35, theme.gui_lineColor());
        graphics.drawCenteredString(font, Component.literal("§l").append(title), width / 2, 12, 0xFFFFFF);
        int panelWidth = Math.min(320, Math.max(180, width - 36));
        int left = (width - panelWidth) / 2;
        section(graphics, theme, "menu.iris.back_items.visibility", left, 83, panelWidth);
        section(graphics, theme, "menu.iris.back_items.mod_items", left, 238, panelWidth);
    }

    @Override
    public void onClose() {
        saveWhitelist();
        minecraft.setScreen(parent);
    }

    private Button toggleButton(int x, int y, int buttonWidth, String key, boolean initial, java.util.function.Consumer<Boolean> setter) {
        AtomicBoolean value = new AtomicBoolean(initial);
        return Button.builder(label(key, initial), button -> {
            boolean enabled = !value.get();
            value.set(enabled);
            setter.accept(enabled);
            ConfigOptions.getInstance().save();
            button.setMessage(label(key, enabled));
        }).bounds(x, y, buttonWidth, BUTTON_HEIGHT).build();
    }

    private void saveWhitelist() {
        if (whitelistField == null) return;
        ConfigOptions config = ConfigOptions.getInstance();
        config.backItemWhitelist = new ArrayList<>(Arrays.stream(whitelistField.getValue().split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .toList());
        config.save();
    }

    private static Component label(String key, boolean enabled) {
        UiTheme theme = UiTheme.getCurrent();
        String color = enabled ? theme.gui_onColor() : theme.gui_offColor();
        return Component.translatable(key, Component.literal(color).append(Component.translatable(enabled ? "menu.iris.on" : "menu.iris.off")));
    }

    private void section(GuiGraphics graphics, UiTheme theme, String key, int x, int y, int sectionWidth) {
        graphics.drawString(font, Component.literal("§8> §7").append(Component.translatable(key)), x, y, 0xFFFFFF, true);
        graphics.fill(x, y + 13, x + sectionWidth, y + 14, theme.gui_separatorLine());
    }
}
