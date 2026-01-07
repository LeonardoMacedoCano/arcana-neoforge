package com.example.arcana.client;

import com.example.arcana.ArcanaMod;
import com.example.arcana.client.gui.DiaryScreen;
import com.example.arcana.client.renderer.DioritePedestalRenderer;
import com.example.arcana.item.DiaryItem;
import com.example.arcana.registry.ModBlockEntities;
import com.example.arcana.util.ArcanaLog;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ArcanaMod.MODID, value = Dist.CLIENT)
public class ClientSetup {

    private static final String MODULE = "CLIENT";

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ArcanaLog.info(MODULE, "Client setup starting");
        event.enqueueWork(() -> DiaryItem.setScreenOpener(player ->
                Minecraft.getInstance().setScreen(new DiaryScreen())
        ));
        ArcanaLog.debug(MODULE, "Client setup finished");
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ArcanaLog.info(MODULE, "Registering DioritePedestalRenderer");
        event.registerBlockEntityRenderer(
                ModBlockEntities.DIORITE_PEDESTAL.get(),
                ctx -> new DioritePedestalRenderer()
        );
        ArcanaLog.debug(MODULE, "DioritePedestalRenderer registered");
    }
}
