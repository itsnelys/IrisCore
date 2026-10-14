package net.opal.iris.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.opal.iris.Iris;
import net.opal.iris.client.backitem.BackItemManager;
import net.opal.iris.server.backitem.BackShieldProtection;

/** Registers Iris packets that are required by both the client and the server. */
public final class IrisNetworkHandler {
    private IrisNetworkHandler() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Iris.MODID).versioned("1");
        registrar.playToServer(BackItemLockPayload.TYPE, BackItemLockPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        BackShieldProtection.updateLockedShield(player, payload.stack());
                    }
                }));
        registrar.playToClient(BackItemDetachPayload.TYPE, BackItemDetachPayload.CODEC,
                (payload, context) -> context.enqueueWork(BackItemManager::detachLockedBackItemAfterBlock));
    }
}
