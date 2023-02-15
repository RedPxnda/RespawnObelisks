package com.redpxnda.respawnobelisks.event;


import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.redpxnda.respawnobelisks.config.ServerConfig;
import com.redpxnda.respawnobelisks.network.*;
import com.redpxnda.respawnobelisks.registry.Registry;
import com.redpxnda.respawnobelisks.registry.blocks.RespawnObeliskBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

public class RespawnObeliskEvents {
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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && !player.isShiftKeyDown()) return;
        if (mc.hitResult instanceof BlockHitResult blockResult && mc.level != null) {
            BlockState blockState = mc.level.getBlockState(blockResult.getBlockPos());
            if (!(blockState.getBlock() instanceof RespawnObeliskBlock)) return;
            else if (!(blockState.getValue(RespawnObeliskBlock.HALF) == DoubleBlockHalf.LOWER)) return;
            mc.level.playSound(player, blockResult.getBlockPos(), SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, 1, 1);
            Packets.sendToServer(new ScrollWheelPacket(event.getScrollDelta(), blockResult));
            event.setCanceled(true);
        }
    }
}
