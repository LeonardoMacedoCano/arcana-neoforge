package com.arcana.mod.registry;

import com.arcana.mod.ArcanaMod;
import com.arcana.mod.content.blockentity.DioritePedestalBlockEntity;
import com.arcana.mod.util.common.ArcanaLog;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ArcanaMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DioritePedestalBlockEntity>> DIORITE_PEDESTAL =
            BLOCK_ENTITIES.register("diorite_pedestal",
                    () -> BlockEntityType.Builder.of(
                            DioritePedestalBlockEntity::new,
                            ModBlocks.DIORITE_PEDESTAL.get()
                    ).build(null)
            );

    public static void register(IEventBus modEventBus) {
        ArcanaLog.info("BLOCK_ENTITIES", "Registering block entities");
        BLOCK_ENTITIES.register(modEventBus);
    }

}
