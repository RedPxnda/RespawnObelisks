package com.redpxnda.respawnobelisks.util;

import com.redpxnda.respawnobelisks.config.RespawnPerkConfig;
import com.redpxnda.respawnobelisks.network.ParticleAnimationPacket;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Map;
import java.util.Random;

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

    public static AABB getAABB(BlockPos pos) {
        return AABB.of(new BoundingBox(
                pos.getX()-10, pos.getY()-10, pos.getZ()-10,
                pos.getX()+10, pos.getY()+10, pos.getZ()+10
        ));
    }

    public static void curseHandler(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state) {
        List<ServerPlayer> players = level.getPlayers(p -> getAABB(player.getBlockX(), player.getBlockY(), player.getBlockZ()).contains(p.getX(), p.getY(), p.getZ()));
        if (!players.contains(player)) players.add(player);
        ModPackets.CHANNEL.sendToPlayers(players, new ParticleAnimationPacket("curse", player.getId(), pos));
    }

    public static boolean shouldEnchantmentApply(ItemStack stack, Random random) {
        if (!RespawnPerkConfig.Enchantment.enableEnchantment) return false;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        return enchantments.containsKey(ModRegistries.obeliskbound.get()) &&
                random.nextInt(100) <= Math.round(enchantments.get(ModRegistries.obeliskbound.get())*RespawnPerkConfig.Enchantment.chancePerLevel)-1;
    }
}
