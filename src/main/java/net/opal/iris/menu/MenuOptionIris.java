package net.opal.iris.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.opal.iris.option.ConfigOptions;
import net.opal.iris.theme.UiTheme;

import java.util.ArrayList;
import java.util.List;

/** The IrisCore options hub. Feature modules add their own page through the registry. */
public final class MenuOptionIris extends Screen {
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int ROW_STEP = 25;
    private static final int HEADER_HEIGHT = 35;
    private static final int MODULE_TOP = 210;
    private static final int FOOTER_TOP_PADDING = 42;

    private final Screen parent;
    private final List<Button> moduleButtons = new ArrayList<>();
    private int moduleScroll;

    public MenuOptionIris(Screen parent) {
        super(Component.translatable("menu.iris.options"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        moduleButtons.clear();
        ConfigOptions config = ConfigOptions.getInstance();
        int width = contentWidth();
        int left = (this.width - width) / 2;
        int halfWidth = (width - BUTTON_GAP) / 2;

        addRenderableWidget(Button.builder(debugText(config.enableDebugChat), button -> {
            config.enableDebugChat = !config.enableDebugChat;
            button.setMessage(debugText(config.enableDebugChat));
            config.save();
        }).tooltip(tooltip("menu.iris.tooltip.debug_chat")).bounds(left, 65, halfWidth, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(themeText(config), button -> {
            config.theme = config.theme.next();
            button.setMessage(themeText(config));
            config.save();
        }).tooltip(tooltip("menu.iris.tooltip.theme")).bounds(left + halfWidth + BUTTON_GAP, 65, halfWidth, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(Component.translatable("iris.support.title"), button ->
                this.minecraft.setScreen(new IrisSupportScreen(this)))
                .bounds(left, 90, width, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(hudText(config.enableIndicators), button -> {
            config.enableIndicators = !config.enableIndicators;
            button.setMessage(hudText(config.enableIndicators));
            config.save();
        }).tooltip(tooltip("menu.iris.tooltip.hud")).bounds(left, 135, halfWidth, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(sideText(config.indicatorPosition), button -> {
            config.indicatorPosition = config.indicatorPosition.next();
            button.setMessage(sideText(config.indicatorPosition));
            config.save();
        }).tooltip(tooltip("menu.iris.tooltip.side")).bounds(left + halfWidth + BUTTON_GAP, 135, halfWidth, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(backItemsText(config.enableBackItems), button -> {
            config.enableBackItems = !config.enableBackItems;
            button.setMessage(backItemsText(config.enableBackItems));
            config.save();
        }).tooltip(tooltip("menu.iris.tooltip.back_items")).bounds(left, 160, width, BUTTON_HEIGHT).build());

        for (IrisModuleOptionsRegistry.Entry entry : IrisModuleOptionsRegistry.entries()) {
            Button button = Button.builder(entry.title(), ignored -> this.minecraft.setScreen(entry.screenFactory().apply(this)))
                    .bounds(left, 0, width, BUTTON_HEIGHT)
                    .build();
            moduleButtons.add(addRenderableWidget(button));
        }

        addRenderableWidget(Button.builder(Component.translatable("menu.iris.return"), ignored -> onClose())
                .tooltip(tooltip("menu.iris.tooltip.return"))
                .bounds(left, this.height - 30, width, BUTTON_HEIGHT).build());
        moduleScroll = Mth.clamp(moduleScroll, 0, maxModuleScroll());
        updateModulePositions();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        UiTheme theme = UiTheme.getCurrent();
        graphics.fill(0, 0, this.width, this.height, theme.gui_bgOverlay());
        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics.fill(0, 0, this.width, HEADER_HEIGHT, theme.gui_barColor());
        graphics.fill(0, HEADER_HEIGHT - 1, this.width, HEADER_HEIGHT, theme.gui_lineColor());
        graphics.drawCenteredString(this.font, Component.literal("§l").append(this.title), this.width / 2, 12, 0xFFFFFF);

        renderFooter(graphics, theme);
        int left = (this.width - contentWidth()) / 2;
        drawSection(graphics, theme, "menu.iris.section.general", left, 48, contentWidth());
        drawSection(graphics, theme, "menu.iris.section.indicators", left, 118, contentWidth());
        drawSection(graphics, theme, "menu.iris.section.modules", left, 193, contentWidth());
        renderModulesScrollbar(graphics, theme, left + contentWidth() + 8);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseY < MODULE_TOP || mouseY > moduleBottom()) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        int previous = moduleScroll;
        moduleScroll = Mth.clamp(moduleScroll - (int) (scrollY * ROW_STEP), 0, maxModuleScroll());
        if (previous != moduleScroll) {
            updateModulePositions();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }

    private void updateModulePositions() {
        int left = (this.width - contentWidth()) / 2;
        int bottom = moduleBottom();
        for (int index = 0; index < moduleButtons.size(); index++) {
            Button button = moduleButtons.get(index);
            int y = MODULE_TOP + index * ROW_STEP - moduleScroll;
            button.setX(left);
            button.setY(y);
            button.setWidth(contentWidth());
            button.visible = y >= MODULE_TOP && y + BUTTON_HEIGHT <= bottom;
            button.active = button.visible;
        }
    }

    private void renderModulesScrollbar(GuiGraphics graphics, UiTheme theme, int x) {
        if (maxModuleScroll() <= 0) return;
        int top = MODULE_TOP;
        int bottom = moduleBottom();
        int trackHeight = bottom - top;
        int contentHeight = moduleButtons.size() * ROW_STEP;
        int thumbHeight = Math.max(18, trackHeight * trackHeight / Math.max(trackHeight, contentHeight));
        int thumbY = top + (int) ((trackHeight - thumbHeight) * (moduleScroll / (float) maxModuleScroll()));
        graphics.fill(x, top, x + 2, bottom, theme.gui_separatorLine());
        graphics.fill(x - 1, thumbY, x + 3, thumbY + thumbHeight, theme.gui_lineColor());
    }

    private void drawSection(GuiGraphics graphics, UiTheme theme, String key, int x, int y, int width) {
        graphics.drawString(this.font, Component.literal("§8> §7").append(Component.translatable(key)), x, y, 0xFFFFFF, true);
        graphics.fill(x, y + 13, x + width, y + 14, theme.gui_separatorLine());
    }

    private void renderFooter(GuiGraphics graphics, UiTheme theme) {
        int footerY = this.height - 40;
        int left = (this.width - contentWidth()) / 2;
        int right = left + contentWidth();
        graphics.fill(0, footerY, left, this.height, theme.gui_barColor());
        graphics.fill(0, footerY, left, footerY + 1, theme.gui_lineColor());
        graphics.fill(right, footerY, this.width, this.height, theme.gui_barColor());
        graphics.fill(right, footerY, this.width, footerY + 1, theme.gui_lineColor());
        graphics.fill(left, footerY, right, this.height - 30, theme.gui_barColor());
        graphics.fill(left, footerY, right, footerY + 1, theme.gui_lineColor());
        graphics.fill(left, this.height - 10, right, this.height, theme.gui_barColor());
    }

    private int contentWidth() {
        return Math.min(200, Math.max(120, this.width - 36));
    }

    private int moduleBottom() {
        return this.height - FOOTER_TOP_PADDING;
    }

    private int maxModuleScroll() {
        return Math.max(0, moduleButtons.size() * ROW_STEP - (moduleBottom() - MODULE_TOP));
    }

    private static Component debugText(boolean enabled) {
        return Component.translatable("menu.iris.debug_chat", state(enabled));
    }

    private static Component themeText(ConfigOptions config) {
        return Component.translatable("menu.iris.theme", colored(UiTheme.getCurrent().gui_valColor(), Component.translatable(config.theme.translationKey())));
    }

    private static Component hudText(boolean enabled) {
        return Component.translatable("menu.iris.hud", state(enabled));
    }

    private static Component sideText(ConfigOptions.IndicatorPosition position) {
        String key = position == ConfigOptions.IndicatorPosition.LEFT ? "menu.iris.left" : "menu.iris.right";
        return Component.translatable("menu.iris.side", colored(UiTheme.getCurrent().gui_valColor(), Component.translatable(key)));
    }

    private static Component backItemsText(boolean enabled) {
        return Component.translatable("menu.iris.back_items", state(enabled));
    }

    private static Component state(boolean enabled) {
        UiTheme theme = UiTheme.getCurrent();
        return colored(enabled ? theme.gui_onColor() : theme.gui_offColor(), Component.translatable(enabled ? "menu.iris.on" : "menu.iris.off"));
    }

    private static Component colored(String color, Component text) {
        return Component.literal(color).append(text);
    }

    private static Tooltip tooltip(String key) {
        return Tooltip.create(Component.translatable(key));
    }
}
