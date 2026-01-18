package com.example.arcana.content.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FoolSoulItem  extends Item {

    public FoolSoulItem(Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack,
                                @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flag) {

        tooltip.add(Component.translatable("item.arcana.fool_soul.tooltip.line1"));
        tooltip.add(Component.translatable("item.arcana.fool_soul.tooltip.line2"));

        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack,
                              @NotNull net.minecraft.world.entity.LivingEntity entity) {
        return 72000;
    }
}
