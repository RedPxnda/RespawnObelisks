package com.redpxnda.respawnobelisks.util;

import com.redpxnda.respawnobelisks.registry.particle.RuneCircleParticle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClientUtils {
    public static final Map<List<Double>, RuneCircleParticle> activeRuneParticles = new HashMap<>();

    public static boolean allowHardcoreRespawn = false;

    public static Map<SpawnPoint, Block> cachedSpawnPointBlocks;
    public static Map<SpawnPoint, ItemStack> cachedSpawnPointItems;
    public static long priorityChangerLookAwayTime;
    public static boolean hasLookedAwayFromPriorityChanger;

    public static int getBoundCompassBarWidth(ItemStack stack) {
        if (Minecraft.getInstance().player != null) {
            Player player = Minecraft.getInstance().player;
            if (player.getCooldowns().isOnCooldown(stack.getItem()))
                return Math.round(13.0f - player.getCooldowns().getCooldownPercent(stack.getItem(), 0) * 13.0f);
        }
        return 0;
    }

    public static boolean isBoundCompassBarVisible(ItemStack stack) {
        if (Minecraft.getInstance().player != null) {
            Player player = Minecraft.getInstance().player;
            if (player.getCooldowns().isOnCooldown(stack.getItem())) return true;
        }
        return false;
    }

    public static void addCompassTooltipLines(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        if (Minecraft.getInstance().player != null) {
            Player player = Minecraft.getInstance().player;
            if (player.getCooldowns().isOnCooldown(itemStack.getItem())) {
                list.add(1,
                        Component
                                .literal(((int) (100-(player.getCooldowns().getCooldownPercent(itemStack.getItem(), 0)*100))) + "% ").withStyle(ChatFormatting.AQUA)
                                .append(Component.translatable("text.respawnobelisks.tooltip.loaded").withStyle(ChatFormatting.DARK_AQUA)));
            }
        }
    }
}
