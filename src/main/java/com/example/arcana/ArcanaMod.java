package com.example.arcana;

import com.example.arcana.registry.ModBlocks;
import com.example.arcana.registry.ModCreativeTabs;
import com.example.arcana.registry.ModItems;
import com.example.arcana.systems.dreams.DreamRegistry;
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
        LOGGER.info("═══════════════════════════════════════");
        LOGGER.info("  Inicializando Arcana Mod v1.0");
        LOGGER.info("  Minecraft 1.21.1 - NeoForge");
        LOGGER.info("═══════════════════════════════════════");

        modEventBus.addListener(this::commonSetup);
        registerModContent(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        LOGGER.info("Registro de componentes concluído com sucesso");
    }

    private void registerModContent(IEventBus modEventBus) {
        LOGGER.debug("Registrando conteúdo do mod...");

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        LOGGER.debug("Conteúdo registrado: Blocks, Items, Creative Tabs");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("═══════════════════════════════════════");
        LOGGER.info("  ARCANA - Common Setup");
        LOGGER.info("  Inicializando sistemas do jogo...");
        LOGGER.info("═══════════════════════════════════════");

        event.enqueueWork(this::initializeSystems);
    }

    private void initializeSystems() {
        LOGGER.debug("Inicializando Dream System...");
        DreamRegistry.init();
        LOGGER.debug("Dream System inicializado com sucesso");

        LOGGER.info("Todos os sistemas inicializados");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("═══════════════════════════════════════");
        LOGGER.info("  ARCANA - Server Starting");
        LOGGER.info("  Preparando mundo do servidor...");
        LOGGER.info("═══════════════════════════════════════");
    }
}