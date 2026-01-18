package com.example.arcana;

import com.example.arcana.content.entity.boss.TheFoolEntity;
import com.example.arcana.registry.*;
import com.example.arcana.systems.dreams.DreamRegistry;
import com.example.arcana.util.common.ArcanaLog;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
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
        logStartup();
        registerLifecycle(modEventBus);
        registerContent(modEventBus);
        registerConfig(modContainer);
        registerForgeEvents();
        logReady();
    }

    private void registerLifecycle(IEventBus modEventBus) {
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onRegisterEntityAttributes);
    }

    private void registerContent(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
    }

    private void registerConfig(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
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

    private void logStartup() {
        ArcanaLog.info("Core", "═══════════════════════════════════════");
        ArcanaLog.info("Core", "Starting Arcana Mod v1.0");
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
