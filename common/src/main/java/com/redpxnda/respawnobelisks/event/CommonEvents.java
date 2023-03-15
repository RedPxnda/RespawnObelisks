package com.redpxnda.respawnobelisks.event;

import com.redpxnda.respawnobelisks.config.ServerConfig;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.SyncEffectsPacket;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.scheduled.ScheduledTasks;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public class CommonEvents {
    public static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean wonGame) {
        if (Platform.isFabric()) { // architectury messed up params on fabric, i have to switch em myself until arch fixes.
            ServerPlayer temp = oldPlayer;
            oldPlayer = newPlayer;
            newPlayer = temp;
        }
        if (wonGame) return;
        if (!oldPlayer.hasEffect(ModRegistries.IMMORTALITY_CURSE.get())) return;
        MobEffectInstance MEI = oldPlayer.getEffect(ModRegistries.IMMORTALITY_CURSE.get());
        if (MEI == null) return;
        int amplifier = MEI.getAmplifier();
        if (amplifier == 0) amplifier--;
        for (int i = 0; i < ServerConfig.curseLevelIncrement; i++) {
            if (ServerConfig.curseMaxLevel-1 > amplifier) amplifier++;
            else break;
        }
        newPlayer.addEffect(new MobEffectInstance(MEI.getEffect(), ServerConfig.curseDuration, amplifier));
    }

    public static void onPlayerRespawn(ServerPlayer player, boolean conqueredEnd) {
        if (player.hasEffect(ModRegistries.IMMORTALITY_CURSE.get())) {
            MobEffectInstance MEI = player.getEffect(ModRegistries.IMMORTALITY_CURSE.get());
            if (MEI == null) return;
            ModPackets.CHANNEL.sendToPlayer(player, new SyncEffectsPacket(MEI.getAmplifier(), MEI.getDuration()));
        }
    }

    public static void init() {
        TickEvent.SERVER_POST.register(ScheduledTasks::onServerTick);
        PlayerEvent.PLAYER_CLONE.register(CommonEvents::onPlayerClone);
        PlayerEvent.PLAYER_RESPAWN.register(CommonEvents::onPlayerRespawn);
    }
}
