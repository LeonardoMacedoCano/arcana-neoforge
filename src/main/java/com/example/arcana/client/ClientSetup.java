package com.example.arcana.client;

import com.example.arcana.ArcanaMod;
import com.example.arcana.client.gui.DiaryScreen;
import com.example.arcana.item.DiaryItem;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = ArcanaMod.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ArcanaMod.LOGGER.info("ARCANA MOD - Client Setup");

        event.enqueueWork(() -> {
            DiaryItem.setScreenOpener(player -> {
                Minecraft.getInstance().setScreen(new DiaryScreen());
            });
        });
    }
}