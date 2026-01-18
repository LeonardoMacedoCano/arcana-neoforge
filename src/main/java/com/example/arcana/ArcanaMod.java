package com.example.arcana;

import com.example.arcana.registry.ModBlockEntities;
import com.example.arcana.registry.ModBlocks;
import com.example.arcana.registry.ModCreativeTabs;
import com.example.arcana.registry.ModItems;
import com.example.arcana.systems.dreams.DreamRegistry;
import com.example.arcana.util.common.ArcanaLog;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(ArcanaMod.MODID)
public class ArcanaMod {
    public static final String MODID = "arcana";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ArcanaMod(IEventBus modEventBus, ModContainer modContainer) {
        ArcanaLog.info("Core", "═══════════════════════════════════════");
        ArcanaLog.info("Core", "Starting Arcana Mod v1.0");
        ArcanaLog.info("Core", "Minecraft 1.21.1 - NeoForge");
        ArcanaLog.info("Core", "═══════════════════════════════════════");

        modEventBus.addListener(this::commonSetup);
        registerModContent(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ArcanaLog.info("Core", "Component registration completed successfully");
    }

    private void registerModContent(IEventBus modEventBus) {
        ArcanaLog.debug("Core", "Registering mod content...");

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        ArcanaLog.debug("Core", "Content registered: Blocks, Items, Creative Tabs");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        ArcanaLog.info("Core", "═══════════════════════════════════════");
        ArcanaLog.info("Core", "ARCANA - Common Setup");
        ArcanaLog.info("Core", "Initializing core systems...");
        ArcanaLog.info("Core", "═══════════════════════════════════════");

        event.enqueueWork(this::initializeSystems);
    }

    private void initializeSystems() {
        ArcanaLog.debug("Core", "Initializing Dream System...");
        DreamRegistry.init();
        ArcanaLog.debug("Core", "Dream System initialized successfully");

        ArcanaLog.info("Core", "All systems initialized successfully");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        ArcanaLog.info("Core", "═══════════════════════════════════════");
        ArcanaLog.info("Core", "ARCANA - Server Starting");
        ArcanaLog.info("Core", "Preparing server world...");
        ArcanaLog.info("Core", "═══════════════════════════════════════");
    }
}
