package com.arcana.mod.util.server;

import java.util.ArrayList;
import java.util.List;

public class DelayedMessageHandler {

    private static final List<DelayedMessageQueue> QUEUES = new ArrayList<>();

    public static void addQueue(DelayedMessageQueue queue) {
        QUEUES.add(queue);
    }

    public static void tick() {
        QUEUES.removeIf(DelayedMessageQueue::tick);
    }

    public static void clear() {
        QUEUES.clear();
    }
}