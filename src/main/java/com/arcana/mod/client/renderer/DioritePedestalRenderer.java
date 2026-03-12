package com.arcana.mod.client.renderer;

import com.arcana.mod.content.blockentity.DioritePedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < be.getContainerSize(); i++) {
            ItemStack stack = be.getItem(i);
            if (!stack.isEmpty()) items.add(stack);
        }
        if (items.isEmpty()) return;

        final float ITEM_Y     = 1.3f;
        final float ITEM_SCALE = 0.22f;

        for (int i = 0; i < items.size(); i++) {
            pose.pushPose();

            float xOffset = computeOffset(i, items.size());

            pose.translate(0.5f + xOffset, ITEM_Y, 0.5f);
            pose.mulPose(Axis.XP.rotationDegrees(-90f));
            pose.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);

            renderer.renderStatic(
                    items.get(i),
                    ItemDisplayContext.NONE,
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

    private float computeOffset(int index, int totalItems) {
        final float SPACING    = 0.28f;
        if (totalItems == 1) return 0;
        float start = -((totalItems - 1) * SPACING) / 2f;
        return start + index * SPACING;
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull DioritePedestalBlockEntity be) {
        return true;
    }
}
