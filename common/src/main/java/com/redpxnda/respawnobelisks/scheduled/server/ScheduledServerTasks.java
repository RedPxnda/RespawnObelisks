package com.redpxnda.respawnobelisks.scheduled.server;

import com.redpxnda.respawnobelisks.scheduled.Task;
import net.minecraft.server.MinecraftServer;

import java.util.HashSet;
import java.util.Set;

public class ScheduledServerTasks {
    public static Set<Task> tasks = new HashSet<>();

    public static void onServerTick(MinecraftServer instance) {
        for (Task task : tasks) {
            task.tick();
        }
        tasks.removeIf(Task::hasBeenExecuted);
    }

    public static void schedule(Task task) {
        tasks.add(task);
    }
}
