package com.example.arcana.client.renderer;

import com.example.arcana.blockentity.DioritePedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

public class DioritePedestalRenderer implements BlockEntityRenderer<DioritePedestalBlockEntity> {

    @Override
    public void render(
            DioritePedestalBlockEntity be,
            float partialTicks,
            @NotNull PoseStack pose,
            @NotNull MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        var renderer = Minecraft.getInstance().getItemRenderer();

        int itemCount = be.getContainerSize();
        float spacing = 0.20f;

        for (int i = 0; i < itemCount; i++) {
            var item = be.getItem(i);
            if (item.isEmpty()) continue;

            pose.pushPose();

            assert be.getLevel() != null;
            var shape = be.getBlockState().getShape(be.getLevel(), be.getBlockPos());
            double height = shape.max(Direction.Axis.Y);

            float xOffset = computeOffset(i, itemCount, spacing);

            pose.translate(0.5 + xOffset, height + 0.02, 0.5);
            pose.scale(0.3f, 0.3f, 0.3f);
            pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90f));

            renderer.renderStatic(
                    item,
                    ItemDisplayContext.FIXED,
                    light,
                    overlay,
                    pose,
                    buffer,
                    be.getLevel(),
                    0
            );

            pose.popPose();
        }
    }

    private float computeOffset(int index, int totalItems, float spacing) {
        if (totalItems == 1) return 0;
        float start = -((totalItems - 1) * spacing) / 2f;
        return start + index * spacing;
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull DioritePedestalBlockEntity be) {
        return true;
    }
}
