package com.abadon.minecontrollers;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {
    public static ServerConfig serverConfig = null;
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec.IntValue MAX_OBS_RANGE = BUILDER
            .comment("Maximum range that microcontroller can observe blocks")
            .defineInRange("max_observable_distance", 128, 0, Integer.MAX_VALUE);
    public static ForgeConfigSpec.IntValue MAX_ENTITY_FIND_RANGE = BUILDER
            .comment("Maximum range that microcontroller can detect entities")
            .defineInRange("max_entity_find_range", 32, 0, Integer.MAX_VALUE);
    public static ForgeConfigSpec.IntValue MAX_PLAYER_FIND_RANGE = BUILDER
            .comment("Maximum range that microcontroller can detect players")
            .defineInRange("max_player_find_range", 32, 0, Integer.MAX_VALUE);
    public static ForgeConfigSpec.IntValue MAX_MC_SOUNDING_RANGE = BUILDER
            .comment("Maximum range that microcontroller can make sounds")
            .defineInRange("max_sounding_range", 32, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
