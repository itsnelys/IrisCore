package net.opal.iris.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.opal.iris.Iris;

/** Informs the client that a rear shield block consumed its BackItems lock. */
public record BackItemDetachPayload() implements CustomPacketPayload {
    public static final Type<BackItemDetachPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Iris.MODID, "back_item_detach"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BackItemDetachPayload> CODEC = StreamCodec.of(
            (buffer, payload) -> { }, buffer -> new BackItemDetachPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
