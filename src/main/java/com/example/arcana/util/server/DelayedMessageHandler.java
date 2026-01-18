package com.example.arcana.util.server;

import java.util.LinkedList;
import java.util.List;

public class DelayedMessageHandler {

    private static final List<DelayedMessageQueue> QUEUES = new LinkedList<>();

    public static void addQueue(DelayedMessageQueue queue) {
        QUEUES.add(queue);
    }

    public static void tick() {
        QUEUES.removeIf(DelayedMessageQueue::tick);
    }
}