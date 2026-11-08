package com.abadon.minecontrollers.network.packets;

import com.abadon.minecontrollers.Minecontrollers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DebugSyncResponsePayload(int address) implements CustomPacketPayload {
    public static final Type<DebugSyncResponsePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Minecontrollers.MODID, "debug_sync_response")
    );
    public static final StreamCodec<FriendlyByteBuf, DebugSyncResponsePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DebugSyncResponsePayload::address,
            DebugSyncResponsePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
