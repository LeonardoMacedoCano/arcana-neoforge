package com.arcana.mod.registry;

import com.arcana.mod.ArcanaMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, ArcanaMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> THE_FOOL_AMBIENT =
            SOUNDS.register("the_fool.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ArcanaMod.MODID, "the_fool.ambient")));

    public static final DeferredHolder<SoundEvent, SoundEvent> THE_FOOL_HURT =
            SOUNDS.register("the_fool.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ArcanaMod.MODID, "the_fool.hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> THE_FOOL_DEATH =
            SOUNDS.register("the_fool.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ArcanaMod.MODID, "the_fool.death")));

    public static final DeferredHolder<SoundEvent, SoundEvent> THE_FOOL_SHOCKWAVE =
            SOUNDS.register("the_fool.shockwave",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ArcanaMod.MODID, "the_fool.shockwave")));

    public static void register(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }
}
