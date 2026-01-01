package com.example.arcana.registry;

import com.example.arcana.ArcanaMod;
import com.example.arcana.item.DiaryItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ArcanaMod.MODID);

    public static final DeferredItem<Item> DIARY_KALIASTRUS = ITEMS.register(
            "diary_kaliastrus",
            () -> new DiaryItem(new Item.Properties().stacksTo(1))
    );

    public static void register(net.neoforged.bus.api.IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        ArcanaMod.LOGGER.debug("Items registrados com sucesso");
    }
}