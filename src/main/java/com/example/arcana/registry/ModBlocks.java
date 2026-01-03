package com.example.arcana.registry;

import com.example.arcana.util.ArcanaLog;
import com.example.arcana.ArcanaMod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

public class ModBlocks {
    private static final String MODULE = "BLOCKS";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ArcanaMod.MODID);

    public static void register(IEventBus modEventBus) {
        ArcanaLog.debug(MODULE, "Blocks register starting");
        BLOCKS.register(modEventBus);
        ArcanaLog.debug(MODULE, "Blocks registered successfully");
    }
}
