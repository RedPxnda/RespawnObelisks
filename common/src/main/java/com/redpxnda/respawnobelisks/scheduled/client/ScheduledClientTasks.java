package com.redpxnda.respawnobelisks.scheduled.client;

import com.redpxnda.respawnobelisks.scheduled.Task;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

import java.util.HashSet;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ScheduledClientTasks {
    public static Set<Task> tasks = new HashSet<>();

    public static void onClientTick(Minecraft instance) {
        for (Task task : tasks) {
            task.tick();
        }
        tasks.removeIf(Task::hasBeenExecuted);
    }

    public static void schedule(Task task) {
        tasks.add(task);
    }
}
