package com.abadon.minecontrollers.network.packets;

import com.abadon.minecontrollers.Minecontrollers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DebugSyncPayload(int address) implements CustomPacketPayload {
    public static final Type<DebugSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Minecontrollers.MODID, "debug_sync")
    );
    public static final StreamCodec<FriendlyByteBuf, DebugSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DebugSyncPayload::address,
            DebugSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}