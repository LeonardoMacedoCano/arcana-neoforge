package com.arcana.mod.registry;

import com.arcana.mod.content.block.DioritePedestalBlock;
import com.arcana.mod.util.common.ArcanaLog;
import com.arcana.mod.ArcanaMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

public class ModBlocks {
    private static final String MODULE = "BLOCKS";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ArcanaMod.MODID);

    public static final DeferredHolder<Block, Block> DIORITE_PEDESTAL =
            BLOCKS.register("diorite_pedestal",
                    () -> new DioritePedestalBlock(BlockBehaviour.Properties.of()
                            .strength(2.5F)
                            .noOcclusion()
                    )
            );

    public static void register(IEventBus modEventBus) {
        ArcanaLog.debug(MODULE, "Blocks register starting");
        BLOCKS.register(modEventBus);
        ArcanaLog.debug(MODULE, "Blocks registered successfully");
    }
}
