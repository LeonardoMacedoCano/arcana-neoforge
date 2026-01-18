package com.example.arcana.content.blockentity;

import com.example.arcana.registry.ModBlockEntities;
import com.example.arcana.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class DioritePedestalBlockEntity extends BlockEntity implements Container {

    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    public DioritePedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DIORITE_PEDESTAL.get(), pos, state);
    }

    public boolean hasBoth() {
        return !items.get(0).isEmpty() && !items.get(1).isEmpty();
    }

    public boolean insertOrToggle(ItemStack stack, Player player) {
        if (stack.isEmpty()) return false;

        if (tryInsert(stack)) return true;

        removeItemToPlayer(player);
        return false;
    }

    public void removeItemToPlayer(Player player) {
        ItemStack removed = tryRemove();
        if (!removed.isEmpty()) player.getInventory().placeItemBackInInventory(removed);
    }

    public boolean tryInsert(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (items.get(0).isEmpty() && stack.is(Items.NETHER_STAR)) {
            setItemAndSync(0, stack.split(1));
            return true;
        }

        if (items.get(1).isEmpty() && stack.is(ModItems.STRANGE_TOTEM.get())) {
            setItemAndSync(1, stack.split(1));
            return true;
        }

        return false;
    }

    public ItemStack tryRemove() {
        if (!items.get(1).isEmpty()) return removeItemAndSync(1);
        if (!items.get(0).isEmpty()) return removeItemAndSync(0);
        return ItemStack.EMPTY;
    }

    public static void tick(Level level, BlockPos pos, BlockState ignoredState, DioritePedestalBlockEntity be) {
        if (level.isClientSide || !be.hasBoth()) return;

        if (!isFireNearby(level, pos)) return;

        be.clearAll();
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0F, Level.ExplosionInteraction.BLOCK);

        spawnPedestalMob(level, pos);
    }

    private static boolean isFireNearby(Level level, BlockPos pos) {
        return level.getBlockState(pos.above()).is(Blocks.FIRE) ||
                level.getBlockState(pos.north()).is(Blocks.FIRE) ||
                level.getBlockState(pos.south()).is(Blocks.FIRE) ||
                level.getBlockState(pos.east()).is(Blocks.FIRE) ||
                level.getBlockState(pos.west()).is(Blocks.FIRE);
    }

    private static void spawnPedestalMob(Level level, BlockPos pos) {
        var entity = EntityType.CHICKEN.create(level);
        if (entity != null) {
            entity.setPos(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5);
            level.addFreshEntity(entity);
        }
    }

    private void setItemAndSync(int index, ItemStack stack) {
        items.set(index, stack);
        sync();
    }

    private ItemStack removeItemAndSync(int index) {
        ItemStack out = items.get(index);
        items.set(index, ItemStack.EMPTY);
        sync();
        return out;
    }

    private void clearAll() {
        items.set(0, ItemStack.EMPTY);
        items.set(1, ItemStack.EMPTY);
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(tag, provider);
        ContainerHelper.saveAllItems(tag, items, provider);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);
        items = NonNullList.withSize(2, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, provider);
    }

    @Override public int getContainerSize() { return 2; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public @NotNull ItemStack getItem(int i) { return items.get(i); }
    @Override public @NotNull ItemStack removeItem(int i, int amt) { return ContainerHelper.removeItem(items, i, amt); }
    @Override public @NotNull ItemStack removeItemNoUpdate(int i) { return items.set(i, ItemStack.EMPTY); }
    @Override public void setItem(int i, @NotNull ItemStack s){ items.set(i,s); }
    @Override public boolean stillValid(@NotNull Player p){ return true; }
    @Override public void clearContent(){ items.clear(); }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide) this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
        return saveWithoutMetadata(provider);
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        loadAdditional(tag, provider);
    }
}
