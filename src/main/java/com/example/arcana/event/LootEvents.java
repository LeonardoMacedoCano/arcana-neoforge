package com.example.arcana.event;

import com.example.arcana.ArcanaMod;
import com.example.arcana.systems.loot.DesertPyramidLootSystem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;

@EventBusSubscriber(modid = ArcanaMod.MODID)
public class LootEvents {

    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent event) {

        if (DesertPyramidLootSystem.isDesertPyramid(event.getName())) {
            event.getTable().addPool(
                    DesertPyramidLootSystem.createStrangeTotemPool()
            );
        }
    }
}
