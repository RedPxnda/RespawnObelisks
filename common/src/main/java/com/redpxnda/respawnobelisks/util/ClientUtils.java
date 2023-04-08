package com.redpxnda.respawnobelisks.util;

import com.redpxnda.respawnobelisks.registry.particle.RuneCircleParticle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientUtils {
    public static List<RuneCircleParticle> activeRuneParticles = new ArrayList<>();
    private static Map<String, Integer> tracker = new HashMap<>();

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

    public static int getTracker(String key) {
        return tracker.getOrDefault(key, -1);
    }

    public static int getOrStartTracker(String key, int amount) {
        if (!tracker.containsKey(key)) tracker.put(key, amount);
        return tracker.get(key);
    }

    public static boolean hasTracker(String key) {
        return tracker.containsKey(key);
    }

    public static void tickTracker(String key) {
        tracker.replace(key, tracker.get(key)+1);
    }

    public static void setTracker(String key, int amount) {
        if (tracker.containsKey(key)) tracker.replace(key, amount);
        else tracker.put(key, amount);
    }
}
