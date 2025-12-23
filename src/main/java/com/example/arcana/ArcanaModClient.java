package com.example.arcana;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = ArcanaMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ArcanaMod.MODID, value = Dist.CLIENT)
public class ArcanaModClient {

    public ArcanaModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        String playerName = Minecraft.getInstance().getUser().getName();
        ArcanaMod.LOGGER.info("═══════════════════════════════════════");
        ArcanaMod.LOGGER.info("  ARCANA - Client Initialization");
        ArcanaMod.LOGGER.info("  Welcome, {}", playerName);
        ArcanaMod.LOGGER.info("═══════════════════════════════════════");
    }
}