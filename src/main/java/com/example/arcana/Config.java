package com.example.arcana;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CHEST_SPAWN_DISTANCE = BUILDER
            .comment("Distance from player where the diary chest can spawn (in blocks)")
            .defineInRange("chestSpawnDistance", 3, 1, 10);

    public static final ModConfigSpec.BooleanValue DEBUG_MODE = BUILDER
            .comment("Enable debug logging for Arcana mod")
            .define("debugMode", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}