package com.example.arcana.persistence;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class DiaryWorldData extends SavedData {

    private boolean structureSpawned = false;
    private BlockPos structurePos = null;

    public static DiaryWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        DiaryWorldData::new,
                        DiaryWorldData::load
                ),
                "arcana.diary_world_state"
        );
    }

    public DiaryWorldData() {}

    public static DiaryWorldData load(CompoundTag tag, HolderLookup.Provider provider) {
        DiaryWorldData state = new DiaryWorldData();
        state.structureSpawned = tag.getBoolean("spawned");

        if (tag.contains("x")) {
            state.structurePos = new BlockPos(
                    tag.getInt("x"),
                    tag.getInt("y"),
                    tag.getInt("z")
            );
        }

        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putBoolean("spawned", structureSpawned);

        if (structurePos != null) {
            tag.putInt("x", structurePos.getX());
            tag.putInt("y", structurePos.getY());
            tag.putInt("z", structurePos.getZ());
        }

        return tag;
    }

    public boolean hasSpawned() {
        return structureSpawned;
    }

    public void setSpawned(BlockPos pos) {
        this.structureSpawned = true;
        this.structurePos = pos;
        setDirty();
    }

    public BlockPos getStructurePos() {
        return structurePos;
    }
}
