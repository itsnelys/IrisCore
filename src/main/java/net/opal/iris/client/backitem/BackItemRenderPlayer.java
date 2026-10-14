package net.opal.iris.client.backitem;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class BackItemRenderPlayer {
    private BackItemRenderPlayer() {}

    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) renderer.addLayer(new BackItemLayer(renderer));
        }
    }
}
