package com.example.arcana.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class DiaryItem extends Item {

    private static final String KEY = "arcana.diary_id";
    private static final String BOUND_KEY = "arcana.diary_bound_player";

    private static Consumer<Player> screenOpener = null;

    public DiaryItem(Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }

    public static void setScreenOpener(Consumer<Player> opener) {
        screenOpener = opener;
    }

    public static ItemStack createBoundDiary(ServerPlayer player, Item diaryItem) {
        ItemStack stack = new ItemStack(diaryItem);
        CompoundTag tag = new CompoundTag();

        tag.putUUID(KEY, UUID.randomUUID());
        tag.putUUID(BOUND_KEY, player.getUUID());

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            if (screenOpener != null) {
                screenOpener.accept(player);
            }
        } else {
            level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PUT, SoundSource.PLAYERS, 0.8F, 1.2F);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack,
                                @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flag) {

        tooltip.add(Component.translatable("diary.arcana.tooltip.line1"));
        tooltip.add(Component.translatable("diary.arcana.tooltip.line2"));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("diary.arcana.tooltip.line3"));

        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack,
                              @NotNull net.minecraft.world.entity.LivingEntity entity) {
        return 72000;
    }
}