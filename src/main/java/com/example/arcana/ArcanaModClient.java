package com.example.arcana;

import com.example.arcana.util.common.ArcanaLog;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = ArcanaMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ArcanaMod.MODID, value = Dist.CLIENT)
public class ArcanaModClient {

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        String playerName = Minecraft.getInstance().getUser().getName();
        ArcanaLog.info("CLIENT", "═══════════════════════════════════════");
        ArcanaLog.info("CLIENT", "ARCANA - Client Initialization");
        ArcanaLog.info("CLIENT", "Welcome, " + playerName);
        ArcanaLog.info("CLIENT", "═══════════════════════════════════════");
    }
}
