package com.arcana.mod;

import com.arcana.mod.content.entity.boss.TheFoolEntity;
import com.arcana.mod.datagen.ArcanaDataGenerators;
import com.arcana.mod.registry.*;
import com.arcana.mod.systems.dreams.DreamRegistry;
import com.arcana.mod.util.common.ArcanaLog;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(ArcanaMod.MODID)
public class ArcanaMod {

    public static final String MODID = "arcana";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ArcanaMod(IEventBus modEventBus, ModContainer modContainer) {
        logStartup(modContainer.getModInfo().getVersion().toString());
        registerLifecycle(modEventBus);
        registerContent(modEventBus);
        registerForgeEvents();
        logReady();
    }

    private void registerLifecycle(IEventBus modEventBus) {
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onRegisterEntityAttributes);
        modEventBus.addListener(ArcanaDataGenerators::onGatherData);
    }

    private void registerContent(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModSounds.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
    }

    private void registerForgeEvents() {
        NeoForge.EVENT_BUS.register(this);
    }

    private void onRegisterEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(
                ModEntities.THE_FOOL.get(),
                TheFoolEntity.createAttributes().build()
        );
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(this::initializeSystems);
    }

    private void initializeSystems() {
        DreamRegistry.init();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        logServerStarting();
    }

    private void logStartup(String version) {
        ArcanaLog.info("Core", "═══════════════════════════════════════");
        ArcanaLog.info("Core", "Starting Arcana Mod v{}", version);
        ArcanaLog.info("Core", "Minecraft 1.21.1 - NeoForge");
        ArcanaLog.info("Core", "═══════════════════════════════════════");
    }

    private void logReady() {
        ArcanaLog.info("Core", "Component registration completed successfully");
    }

    private void logServerStarting() {
        ArcanaLog.info("Core", "═══════════════════════════════════════");
        ArcanaLog.info("Core", "ARCANA - Server Starting");
        ArcanaLog.info("Core", "Preparing server world...");
        ArcanaLog.info("Core", "═══════════════════════════════════════");
    }
}
