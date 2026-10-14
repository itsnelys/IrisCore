package net.opal.iris.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.opal.iris.Iris;

/** Client sync for the item currently displayed on the local player's back. */
public record BackItemLockPayload(ItemStack stack) implements CustomPacketPayload {
    public static final Type<BackItemLockPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Iris.MODID, "back_item_lock"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BackItemLockPayload> CODEC = StreamCodec.of(
            (buffer, payload) -> ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, payload.stack),
            buffer -> new BackItemLockPayload(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
