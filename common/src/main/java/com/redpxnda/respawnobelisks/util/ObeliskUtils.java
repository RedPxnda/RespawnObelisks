package com.redpxnda.respawnobelisks.util;

import com.redpxnda.nucleus.math.MathUtil;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.facet.kept.KeptRespawnItems;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.ParticleAnimationPacket;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class ObeliskUtils {
    public static Box getAABB(double x, double y, double z) {
        return Box.from(new BlockBox(
                (int) (x-10), (int) (y-10), (int) (z-10),
                (int) (x+10), (int) (y+10), (int) (z+10)
        ));
    }

    public static Box getAABB(float x, float y, float z) {
        return Box.from(new BlockBox(
                (int) (x-10), (int) (y-10), (int) (z-10),
                (int) (x+10), (int) (y+10), (int) (z+10)
        ));
    }

    public static Box getAABB(BlockPos pos) {
        return Box.from(new BlockBox(
                pos.getX()-10, pos.getY()-10, pos.getZ()-10,
                pos.getX()+10, pos.getY()+10, pos.getZ()+10
        ));
    }

    public static void curseHandler(ServerWorld level, ServerPlayerEntity player, BlockPos pos, BlockState state) {
        List<ServerPlayerEntity> players = level.getPlayers(p -> getAABB(player.getBlockX(), player.getBlockY(), player.getBlockZ()).contains(p.getX(), p.getY(), p.getZ()));
        if (!players.contains(player)) players.add(player);
        ModPackets.CHANNEL.sendToPlayers(players, new ParticleAnimationPacket("curse", player.getId(), pos));
    }

    public static boolean shouldSaveItem(boolean enabled, double chance, ItemStack stack) {
        return
                (enabled && MathUtil.random.nextInt(100) < chance) ||
                shouldEnchantmentApply(stack, MathUtil.random);
    }

    public static void restoreSavedItems(ServerPlayerEntity player) {
        KeptRespawnItems items = KeptRespawnItems.KEY.get(player);
        if (items == null) return;
        items.restore(player);
        //if (has && player instanceof ServerPlayerEntity sp) ModRegistries.keepItemsCriterion.trigger(sp); // todo reimplement
    }

    public static void scatterSavedItems(ServerPlayerEntity player) {
        KeptRespawnItems items = KeptRespawnItems.KEY.get(player);
        if (items == null) return;
        items.scatter(player.getX(), player.getY(), player.getZ(), player);
    }

    public static boolean shouldEnchantmentApply(ItemStack stack, Random random) {
        if (!RespawnObelisksConfig.INSTANCE.respawnPerks.enchantment.enableEnchantment) return false;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
        return enchantments.containsKey(ModRegistries.obeliskbound.get()) &&
                random.nextInt(100) <= Math.round(enchantments.get(ModRegistries.obeliskbound.get())*RespawnObelisksConfig.INSTANCE.respawnPerks.enchantment.chancePerLevel)-1;
    }
}
