package com.redpxnda.respawnobelisks.scheduled;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.MinecraftServer;

import java.util.HashSet;
import java.util.Set;

public class ScheduledTasks {
    public static Set<ScheduledRespawnAnchorTask> scheduledRespawnAnchorTasks = new HashSet<>();

    public static void onServerTick(MinecraftServer instance) {
        for (ScheduledRespawnAnchorTask task : scheduledRespawnAnchorTasks) {
            task.tick();
        }
        scheduledRespawnAnchorTasks.removeIf(ScheduledRespawnAnchorTask::hasBeenExecuted);
    }

    public static void schedule(ScheduledRespawnAnchorTask task) {
        scheduledRespawnAnchorTasks.add(task);
    }
}
