package com.example.arcana.client;

import com.example.arcana.ArcanaMod;
import com.example.arcana.client.gui.DiaryScreen;
import com.example.arcana.client.model.TheFoolModel;
import com.example.arcana.client.renderer.DioritePedestalRenderer;
import com.example.arcana.client.renderer.TheFoolRenderer;
import com.example.arcana.content.item.DiaryItem;
import com.example.arcana.registry.ModBlockEntities;
import com.example.arcana.registry.ModEntities;
import com.example.arcana.util.common.ArcanaLog;
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
        ArcanaLog.info(MODULE, "Registering block entity renderers");

        event.registerBlockEntityRenderer(
                ModBlockEntities.DIORITE_PEDESTAL.get(),
                ctx -> new DioritePedestalRenderer()
        );

        ArcanaLog.info(MODULE, "Registering entity renderers");
        event.registerEntityRenderer(
                ModEntities.THE_FOOL.get(),
                TheFoolRenderer::new
        );

        ArcanaLog.debug(MODULE, "All renderers registered");
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ArcanaLog.info(MODULE, "Registering model layers");

        event.registerLayerDefinition(
                TheFoolModel.LAYER_LOCATION,
                TheFoolModel::createBodyLayer
        );

        ArcanaLog.debug(MODULE, "Model layers registered");
    }
}
