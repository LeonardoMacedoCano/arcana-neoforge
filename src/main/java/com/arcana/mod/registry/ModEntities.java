package com.arcana.mod.registry;

import com.arcana.mod.ArcanaMod;
import com.arcana.mod.content.entity.boss.TheFoolEntity;
import com.arcana.mod.util.common.ArcanaLog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ArcanaMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<TheFoolEntity>> THE_FOOL =
            ENTITIES.register("the_fool", () -> EntityType.Builder.of(
                            TheFoolEntity::new,
                            MobCategory.MONSTER
                    )
                    .sized(1.2F, 2.8F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .canSpawnFarFromPlayer()
                    .build("the_fool"));

    public static void register(IEventBus modEventBus) {
        ArcanaLog.info("ENTITIES", "Registering entities");
        ENTITIES.register(modEventBus);
    }
}