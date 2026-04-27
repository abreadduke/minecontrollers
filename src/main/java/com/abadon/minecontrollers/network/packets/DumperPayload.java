package com.abadon.minecontrollers.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.abadon.minecontrollers.Minecontrollers;

public record DumperPayload(int from, int to) implements CustomPacketPayload {
    public static final Type<DumperPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Minecontrollers.MODID, "dumper_packet")
    );
    public static final StreamCodec<FriendlyByteBuf, DumperPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DumperPayload::from,
            ByteBufCodecs.VAR_INT, DumperPayload::to,
            DumperPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}