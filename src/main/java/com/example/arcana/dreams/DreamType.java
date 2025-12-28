package com.example.arcana.dreams;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface DreamType {

    String getName();

    boolean shouldTrigger(ServerPlayer player, ServerLevel level);

    void runDream(ServerPlayer player, ServerLevel level);
}
