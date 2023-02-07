package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.registry.blocks.RespawnObeliskBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ScrollWheelPacket {
    private double delta;
    private BlockHitResult hitResult;

    public ScrollWheelPacket(double direction, BlockHitResult hitResult) {
        this.delta = direction;
        this.hitResult = hitResult;
    }

    public ScrollWheelPacket(FriendlyByteBuf buffer) {
        delta = buffer.readDouble();
        hitResult = buffer.readBlockHitResult();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeDouble(delta);
        buffer.writeBlockHitResult(hitResult);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            BlockPos blockPos = hitResult.getBlockPos();
            if (context.getSender() != null) {
                ServerPlayer player = context.getSender();
                Level level = player.getLevel();
                BlockState state = level.getBlockState(blockPos);
                if (state.getBlock() instanceof RespawnObeliskBlock && state.getValue(RespawnObeliskBlock.HALF) == DoubleBlockHalf.LOWER) {
                    Direction cardinal;
                    if (delta == -1) cardinal = state.getValue(RespawnObeliskBlock.RESPAWN_SIDE).getCounterClockWise();
                    else cardinal = state.getValue(RespawnObeliskBlock.RESPAWN_SIDE).getClockWise();
                    /*int degrees;
                    if (cardinal == Direction.NORTH) degrees = 180;
                    else if (cardinal == Direction.EAST) degrees = -90;
                    else if (cardinal == Direction.SOUTH) degrees = 0;
                    else degrees = 90;*/
                    level.setBlock(blockPos, state.setValue(RespawnObeliskBlock.RESPAWN_SIDE, cardinal), 3);
                    level.setBlock(blockPos.above(), level.getBlockState(blockPos.above()).setValue(RespawnObeliskBlock.RESPAWN_SIDE, cardinal), 3);
                    //player.setRespawnPosition(level.dimension(), blockPos.relative(cardinal), degrees, true, false);
                    player.sendSystemMessage(Component.literal("Spawn side: " + cardinal.getName()), true);
                }
            }
        });
    }
}
