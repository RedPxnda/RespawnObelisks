package com.redpxnda.respawnobelisks.util;

import com.redpxnda.respawnobelisks.network.FirePackMethodPacket;
import com.redpxnda.respawnobelisks.network.ModPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

import java.util.List;

import static com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock.PACK;

public class ObeliskUtils {
    public static AABB getAABB(double x, double y, double z) {
        return AABB.of(new BoundingBox(
                (int) (x-10), (int) (y-10), (int) (z-10),
                (int) (x+10), (int) (y+10), (int) (z+10)
        ));
    }

    public static AABB getAABB(float x, float y, float z) {
        return AABB.of(new BoundingBox(
                (int) (x-10), (int) (y-10), (int) (z-10),
                (int) (x+10), (int) (y+10), (int) (z+10)
        ));
    }

    public static void curseHandler(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state) {
        List<ServerPlayer> players = level.getPlayers(p -> getAABB(player.getBlockX(), player.getBlockY(), player.getBlockZ()).contains(p.getX(), p.getY(), p.getZ()));
        if (!players.contains(player)) players.add(player);
        ModPackets.CHANNEL.sendToPlayers(players, new FirePackMethodPacket("curse", player.getId(), state.getValue(PACK), pos));
        state.getValue(PACK).particleHandler.curseServerHandler(level, player, pos);
    }

    public static int getTotalXpForLevel(int level) {
        if (level < 17) return level * level + 6 * level;
        if (level < 32) return Mth.floor(2.5 * level * level - 40.5 * level + 360);
        return Mth.floor(4.5 * level * level - 162.5 * level + 2220);
    }
}
