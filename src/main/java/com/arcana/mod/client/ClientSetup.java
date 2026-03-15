package com.arcana.mod.client;

import com.arcana.mod.client.gui.DiaryScreen;
import com.arcana.mod.client.model.TheFoolModel;
import com.arcana.mod.client.renderer.DioritePedestalRenderer;
import com.arcana.mod.client.renderer.TheFoolRenderer;
import com.arcana.mod.content.item.DiaryItem;
import com.arcana.mod.registry.ModBlockEntities;
import com.arcana.mod.registry.ModEntities;
import com.arcana.mod.util.common.ArcanaLog;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ClientSetup {

    private static final String MODULE = "CLIENT";

    public static void onClientSetup(FMLClientSetupEvent event) {
        ArcanaLog.info(MODULE, "Client setup starting");
        ArcanaLog.info(MODULE, "Welcome, {}", Minecraft.getInstance().getUser().getName());
        event.enqueueWork(() -> DiaryItem.setScreenOpener(player ->
                Minecraft.getInstance().setScreen(new DiaryScreen())
        ));
        ArcanaLog.debug(MODULE, "Client setup finished");
    }

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

    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ArcanaLog.info(MODULE, "Registering model layers");

        event.registerLayerDefinition(
                TheFoolModel.LAYER_LOCATION,
                TheFoolModel::createBodyLayer
        );

        ArcanaLog.debug(MODULE, "Model layers registered");
    }
}
