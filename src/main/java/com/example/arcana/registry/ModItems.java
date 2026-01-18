package com.example.arcana.registry;

import com.example.arcana.content.item.FoolSoulItem;
import com.example.arcana.content.item.StrangeTotemItem;
import com.example.arcana.util.common.ArcanaLog;
import com.example.arcana.ArcanaMod;
import com.example.arcana.content.item.DiaryItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

public class ModItems {
    private static final String MODULE = "ITEMS";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ArcanaMod.MODID);

    public static final DeferredItem<Item> DIARY_KALIASTRUS =
            ITEMS.register("diary_kaliastrus",
                () -> new DiaryItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<Item> STRANGE_TOTEM =
            ITEMS.register("strange_totem",
                    () -> new StrangeTotemItem(new Item.Properties()));

    public static final DeferredHolder<Item, BlockItem> DIORITE_PEDESTAL =
            ITEMS.register("diorite_pedestal",
                    () -> new BlockItem(ModBlocks.DIORITE_PEDESTAL.get(),
                            new Item.Properties())
            );

    public static final DeferredItem<Item> FOOL_SOUL =
            ITEMS.register("fool_soul",
                    () -> new FoolSoulItem(new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        ArcanaLog.debug(MODULE, "Items register starting");
        ITEMS.register(modEventBus);
        ArcanaLog.debug(MODULE, "Items registered successfully");
    }
}
