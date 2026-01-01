package com.example.arcana.registry;

import com.example.arcana.ArcanaMod;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ArcanaMod.MODID);

    public static void register(net.neoforged.bus.api.IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ArcanaMod.LOGGER.debug("Blocks registrados com sucesso");
    }
}