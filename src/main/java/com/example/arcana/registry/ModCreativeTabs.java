package com.example.arcana.registry;

import com.example.arcana.util.common.ArcanaLog;
import com.example.arcana.ArcanaMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

public class ModCreativeTabs {
    private static final String MODULE = "CREATIVE_TABS";
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArcanaMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ARCANA_TAB =
            CREATIVE_MODE_TABS.register("arcana_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.arcana"))
                    .icon(() -> new ItemStack(ModItems.DIARY_KALIASTRUS.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.DIARY_KALIASTRUS.get());
                        output.accept(ModItems.STRANGE_TOTEM.get());
                        output.accept(ModItems.DIORITE_PEDESTAL_ITEM.get());
                    })
                    .build()
            );

    public static void register(IEventBus modEventBus) {
        ArcanaLog.debug(MODULE, "Creative Tabs register starting");
        CREATIVE_MODE_TABS.register(modEventBus);
        ArcanaLog.debug(MODULE, "Creative Tabs registered successfully");
    }
}
