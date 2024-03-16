package com.redpxnda.respawnobelisks.network;

import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import dev.architectury.networking.NetworkManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

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

    public ScrollWheelPacket(PacketByteBuf buffer) {
        delta = buffer.readDouble();
        hitResult = buffer.readBlockHitResult();
        isUpper = buffer.readBoolean();
    }

    public void toBytes(PacketByteBuf buffer) {
        buffer.writeDouble(delta);
        buffer.writeBlockHitResult(hitResult);
        buffer.writeBoolean(isUpper);
    }

    public void handle(Supplier<NetworkManager.PacketContext> supplier) {
        NetworkManager.PacketContext context = supplier.get();
        supplier.get().queue(() -> {
            BlockPos blockPos = hitResult.getBlockPos();
            if (isUpper) blockPos = blockPos.down();
            if (context.getPlayer() != null && context.getPlayer() instanceof ServerPlayerEntity player) {
                World level = player.getWorld();
                BlockState state = level.getBlockState(blockPos);
                if (state.getBlock() instanceof RespawnObeliskBlock && state.get(RespawnObeliskBlock.HALF) == DoubleBlockHalf.LOWER) {
                    Direction cardinal;
                    if (delta == -1)
                        cardinal = state.get(RespawnObeliskBlock.RESPAWN_SIDE).rotateYCounterclockwise();
                    else cardinal = state.get(RespawnObeliskBlock.RESPAWN_SIDE).rotateYClockwise();
                    level.setBlockState(blockPos, state.with(RespawnObeliskBlock.RESPAWN_SIDE, cardinal), 3);
                    level.setBlockState(blockPos.up(), level.getBlockState(blockPos.up()).with(RespawnObeliskBlock.RESPAWN_SIDE, cardinal), 3);
                    player.sendMessageToClient(Text.literal("Spawn side: " + cardinal.getName()), true);
                }
            }
        });
    }
}
