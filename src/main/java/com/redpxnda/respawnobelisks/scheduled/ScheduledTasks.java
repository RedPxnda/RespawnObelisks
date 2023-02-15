package com.redpxnda.respawnobelisks.scheduled;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "respawnobelisks")
public class ScheduledTasks {
    public static Set<ScheduledRespawnAnchorTask> scheduledRespawnAnchorTasks = new HashSet<>();

    @SubscribeEvent
    static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.END)) {
            for (ScheduledRespawnAnchorTask task : scheduledRespawnAnchorTasks) {
                task.tick();
            }
            scheduledRespawnAnchorTasks.removeIf(ScheduledRespawnAnchorTask::hasBeenExecuted);
        }
    }

    public static void schedule(ScheduledRespawnAnchorTask task) {
        scheduledRespawnAnchorTasks.add(task);
    }
}
