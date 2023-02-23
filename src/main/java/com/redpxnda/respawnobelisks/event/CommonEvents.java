package com.redpxnda.respawnobelisks.event;


import com.redpxnda.respawnobelisks.config.ServerConfig;
import com.redpxnda.respawnobelisks.network.*;
import com.redpxnda.respawnobelisks.registry.Registry;
import com.redpxnda.respawnobelisks.registry.blocks.RespawnObeliskBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonEvents {
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        if (!(event.getOriginal() instanceof ServerPlayer originalPlayer)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!originalPlayer.hasEffect(Registry.IMMORTALITY_CURSE.get())) return;
        MobEffectInstance MEI = originalPlayer.getEffect(Registry.IMMORTALITY_CURSE.get());
        if (MEI == null) return;
        int amp = MEI.getAmplifier();
        if (amp == 0) amp--;
        for (int i = 0; i < ServerConfig.CURSE_LEVEL_INCREMENT.get(); i++) {
            if (ServerConfig.MAX_CURSE_LEVEL.get()-1 > amp) amp++;
            else break;
        }
        player.addEffect(new MobEffectInstance(MEI.getEffect(), ServerConfig.CURSE_DURATION.get(), amp));
        //Packets.sendToPlayer(new SyncMobEffectPacket("respawnobelisks:curse_of_immortality", ServerConfig.CURSE_DURATION.get(), amp), player);
    }
}
