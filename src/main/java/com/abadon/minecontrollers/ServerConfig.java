package com.abadon.minecontrollers;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    public static ServerConfig serverConfig = null;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static ModConfigSpec.IntValue MAX_OBS_RANGE = BUILDER
            .comment("Maximum range that microcontroller can observe blocks")
            .defineInRange("max_observable_distance", 128, 0, 512);
    public static ModConfigSpec.IntValue MAX_ENTITY_FIND_RANGE = BUILDER
            .comment("Maximum range that microcontroller can detect entities")
            .defineInRange("max_entity_find_range", 32, 0, 512);
    public static ModConfigSpec.IntValue MAX_PLAYER_FIND_RANGE = BUILDER
            .comment("Maximum range that microcontroller can detect players")
            .defineInRange("max_player_find_range", 32, 0, 512);
    public static ModConfigSpec.IntValue MAX_MC_SOUNDING_RANGE = BUILDER
            .comment("Maximum range that microcontroller can make sounds")
            .defineInRange("max_sounding_range", 32, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec SPEC = BUILDER.build();
}
