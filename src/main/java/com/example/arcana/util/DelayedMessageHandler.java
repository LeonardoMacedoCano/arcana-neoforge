package com.example.arcana.util;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.LinkedList;
import java.util.List;

@EventBusSubscriber
public class DelayedMessageHandler {

    private static final List<DelayedMessageQueue> QUEUES = new LinkedList<>();

    public static void addQueue(DelayedMessageQueue queue) {
        QUEUES.add(queue);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        QUEUES.removeIf(DelayedMessageQueue::tick);
    }
}