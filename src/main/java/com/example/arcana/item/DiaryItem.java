package com.example.arcana.item;

import com.example.arcana.client.gui.DiaryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DiaryItem extends Item {

    public DiaryItem(Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            Minecraft.getInstance().setScreen(new DiaryScreen());
        } else {
            level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PUT, SoundSource.PLAYERS, 0.8F, 1.2F);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("diary.arcana.tooltip.line1"));
        tooltipComponents.add(Component.translatable("diary.arcana.tooltip.line2"));
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.translatable("diary.arcana.tooltip.line3"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull net.minecraft.world.entity.LivingEntity entity) {
        return 72000;
    }
}