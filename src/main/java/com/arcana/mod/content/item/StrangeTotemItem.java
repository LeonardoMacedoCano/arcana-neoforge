package com.arcana.mod.content.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StrangeTotemItem extends Item {

    public StrangeTotemItem(Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack,
                                @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flag) {
        if (FMLEnvironment.dist != Dist.CLIENT) return;

        tooltip.add(styledLine(
                caesarShift(Component.translatable("item.arcana.strange_totem.tooltip.line1").getString()),
                0xC2A157));
        tooltip.add(styledLine(
                caesarShift(Component.translatable("item.arcana.strange_totem.tooltip.line2").getString()),
                0xB89445));
        tooltip.add(styledLine(
                caesarShift(Component.translatable("item.arcana.strange_totem.tooltip.line3").getString()),
                0xA67C2C));

        super.appendHoverText(stack, context, tooltip, flag);
    }

    private static Component styledLine(String text, int color) {
        return Component.literal(text).withStyle(style -> style
                .withItalic(true)
                .withFont(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "uniform"))
                .withColor(color));
    }

    private String caesarShift(String text) {
        StringBuilder s = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                s.append((char) ('A' + (c - 'A' + 7) % 26));
            } else if (c >= 'a' && c <= 'z') {
                s.append((char) ('a' + (c - 'a' + 7) % 26));
            } else {
                s.append(c);
            }
        }
        return s.toString();
    }
}
