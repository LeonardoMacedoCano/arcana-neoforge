package com.example.arcana.client.renderer;

import com.example.arcana.ArcanaMod;
import com.example.arcana.client.model.TheFoolModel;
import com.example.arcana.content.entity.boss.TheFoolEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TheFoolRenderer extends MobRenderer<TheFoolEntity, TheFoolModel> {

    private static final float BOSS_SCALE = 1.2F;

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ArcanaMod.MODID,
                    "textures/entity/the_fool.png"
            );

    public TheFoolRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new TheFoolModel(context.bakeLayer(TheFoolModel.LAYER_LOCATION)),
                0.8F
        );
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull TheFoolEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(
            @NotNull TheFoolEntity entity,
            @NotNull PoseStack poseStack,
            float partialTickTime
    ) {
        poseStack.scale(BOSS_SCALE, BOSS_SCALE, BOSS_SCALE);
    }
}
