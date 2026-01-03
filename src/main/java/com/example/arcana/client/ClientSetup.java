package com.example.arcana.client;

import com.example.arcana.ArcanaMod;
import com.example.arcana.client.gui.DiaryScreen;
import com.example.arcana.item.DiaryItem;
import com.example.arcana.util.ArcanaLog;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = ArcanaMod.MODID, value = Dist.CLIENT)
public class ClientSetup {
    private static final String MODULE = "CLIENT";

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ArcanaLog.info(MODULE, "Client setup starting");
        event.enqueueWork(() -> {
            DiaryItem.setScreenOpener(player -> Minecraft.getInstance().setScreen(new DiaryScreen()));
        });
        ArcanaLog.debug(MODULE, "Client setup started successfully");
    }
}
