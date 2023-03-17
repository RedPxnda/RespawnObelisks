package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.particle.ParticlePack;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Supplier;

public class ScrollWheelPacket {
    private final double delta;
    private final BlockHitResult hitResult;
    private final boolean isUpper;

    public ScrollWheelPacket(double direction, BlockHitResult hitResult, boolean isUpper) {
        this.delta = direction;
        this.hitResult = hitResult;
        this.isUpper = isUpper;
    }

    public ScrollWheelPacket(FriendlyByteBuf buffer) {
        delta = buffer.readDouble();
        hitResult = buffer.readBlockHitResult();
        isUpper = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeDouble(delta);
        buffer.writeBlockHitResult(hitResult);
        buffer.writeBoolean(isUpper);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        NetworkManager.PacketContext context = supplier.get();
        supplier.get().queue(() -> {
            BlockPos blockPos = hitResult.getBlockPos();
            if (isUpper) blockPos = blockPos.below();
            if (context.getPlayer() != null && context.getPlayer() instanceof ServerPlayer player) {
                Level level = player.getLevel();
                BlockState state = level.getBlockState(blockPos);
                if (state.getBlock() instanceof RespawnObeliskBlock && state.getValue(RespawnObeliskBlock.HALF) == DoubleBlockHalf.LOWER) {
                    if (!isUpper) {
                        Direction cardinal;
                        if (delta == -1)
                            cardinal = state.getValue(RespawnObeliskBlock.RESPAWN_SIDE).getCounterClockWise();
                        else cardinal = state.getValue(RespawnObeliskBlock.RESPAWN_SIDE).getClockWise();
                        level.setBlock(blockPos, state.setValue(RespawnObeliskBlock.RESPAWN_SIDE, cardinal), 3);
                        level.setBlock(blockPos.above(), level.getBlockState(blockPos.above()).setValue(RespawnObeliskBlock.RESPAWN_SIDE, cardinal), 3);
                        player.sendSystemMessage(Component.literal("Spawn side: " + cardinal.getName()), true);
                    } else {
                        List<ParticlePack> list = Arrays.stream(ParticlePack.values()).toList();
                        ParticlePack pack = state.getValue(RespawnObeliskBlock.PACK);
                        int index = list.indexOf(pack);
                        if (list.size() > index+1 && delta > 0) pack = list.get(index+1);
                        else if (index - 1 >= 0 && delta < 0) pack = list.get(index-1);
                        level.setBlock(blockPos, state.setValue(RespawnObeliskBlock.PACK, pack), 3);
                        player.sendSystemMessage(Component.literal("Selected pack: " + pack.getSerializedName()), true);
                    }
                }
            }
        });
    }
}
