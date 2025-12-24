package com.redpxnda.respawnobelisks.util;

import com.redpxnda.nucleus.math.MathUtil;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.facet.kept.KeptRespawnItems;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.ParticleAnimationPacket;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
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

    public static boolean shouldSaveItem(boolean enabled, double chance, ItemStack stack) {
        if (stack.isEmpty()) return false;
        return
                (enabled && MathUtil.random.nextInt(100) < chance) ||
                shouldEnchantmentApply(stack, MathUtil.random);
    }

    public static void restoreSavedItems(ServerPlayer oldPlayer, ServerPlayer player) {
        KeptRespawnItems items = KeptRespawnItems.KEY.get(player);
        if (items == null) return;
        if (!items.isEmpty())
            ModRegistries.keepItemsCriterion.trigger(player);
        items.restore(oldPlayer, player);
    }

    public static void scatterSavedItems(ServerPlayer player) {
        KeptRespawnItems items = KeptRespawnItems.KEY.get(player);
        if (items == null) return;
        items.scatter(player.getX(), player.getY(), player.getZ(), player);
    }

    public static void givePlayerSavedItem(ServerPlayer player, ItemStack stack, BlockPos respawnPos) {
        Inventory inv = player.getInventory();
        while (!stack.isEmpty()) {
            int i = inv.getSlotWithRemainingSpace(stack);
            if (i == -1) {
                i = inv.getFreeSlot();
            }
            if (i == -1) {
                ItemEntity entity = player.drop(stack, false);
                if (entity != null && respawnPos != null)
                    entity.setPos(respawnPos.getX()+0.5, respawnPos.getY()+2.5, respawnPos.getZ()+0.5);
                break;
            }
            int j = stack.getMaxStackSize() - inv.getItem(i).getCount();
            if (!inv.add(i, stack.split(j))) continue;
            player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, i, inv.getItem(i)));
        }
    }

    public static boolean shouldEnchantmentApply(ItemStack stack, Random random) {
        if (!RespawnObelisksConfig.INSTANCE.respawnPerks.enchantment.enableEnchantment) return false;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        return enchantments.containsKey(ModRegistries.obeliskbound.get()) &&
                random.nextInt(100) <= Math.round(enchantments.get(ModRegistries.obeliskbound.get())*RespawnObelisksConfig.INSTANCE.respawnPerks.enchantment.chancePerLevel)-1;
    }
}
