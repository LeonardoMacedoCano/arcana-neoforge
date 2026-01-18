package com.example.arcana.util.server;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayDeque;
import java.util.Queue;

public class DelayedMessageQueue {

    private final ServerPlayer player;
    private final Queue<Component> messages = new ArrayDeque<>();
    private final int delayTicks;
    private int timer = 0;

    public DelayedMessageQueue(ServerPlayer player, int delayTicks) {
        this.player = player;
        this.delayTicks = delayTicks;
    }

    public void addMessage(Component message) {
        messages.add(message);
    }

    public boolean tick() {
        if (messages.isEmpty()) return true;

        timer++;

        if (timer >= delayTicks) {
            timer = 0;
            player.sendSystemMessage(messages.poll());
        }

        return messages.isEmpty();
    }
}