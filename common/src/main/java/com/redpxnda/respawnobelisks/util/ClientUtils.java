package com.redpxnda.respawnobelisks.util;

import com.redpxnda.respawnobelisks.registry.particle.RuneCircleParticle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientUtils {
    public static final Map<List<Double>, RuneCircleParticle> activeRuneParticles = new HashMap<>();
    private static final Map<String, Integer> genericTracker = new HashMap<>();

    public static boolean allowHardcoreRespawn = false;

    public static List<SpawnPoint> allCachedSpawnPoints;
    public static Map<SpawnPoint, Item> cachedSpawnPointItems;
    public static SpawnPoint focusedPriorityChanger;
    public static long priorityChangerLookAwayTime;
    public static int priorityChangerIndex;

    public static int getBoundCompassBarWidth(ItemStack stack) {
        if (MinecraftClient.getInstance().player != null) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player.getItemCooldownManager().isCoolingDown(stack.getItem()))
                return Math.round(13.0f - player.getItemCooldownManager().getCooldownProgress(stack.getItem(), 0) * 13.0f);
        }
        return 0;
    }

    public static boolean isBoundCompassBarVisible(ItemStack stack) {
        if (MinecraftClient.getInstance().player != null) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player.getItemCooldownManager().isCoolingDown(stack.getItem())) return true;
        }
        return false;
    }

    public static void addCompassTooltipLines(ItemStack itemStack, @Nullable World level, List<Text> list, TooltipContext tooltipFlag) {
        if (MinecraftClient.getInstance().player != null) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
                list.add(1,
                        Text
                                .literal(((int) (100-(player.getItemCooldownManager().getCooldownProgress(itemStack.getItem(), 0)*100))) + "% ").formatted(Formatting.AQUA)
                                .append(Text.translatable("text.respawnobelisks.tooltip.loaded").formatted(Formatting.DARK_AQUA)));
            }
        }
    }

    public static int getTracker(String key) {
        return genericTracker.getOrDefault(key, -1);
    }

    public static int getOrStartTracker(String key, int amount) {
        if (!genericTracker.containsKey(key)) genericTracker.put(key, amount);
        return genericTracker.get(key);
    }

    public static boolean hasTracker(String key) {
        return genericTracker.containsKey(key);
    }

    public static void tickTracker(String key) {
        genericTracker.replace(key, genericTracker.get(key)+1);
    }

    public static void setTracker(String key, int amount) {
        if (genericTracker.containsKey(key)) genericTracker.replace(key, amount);
        else genericTracker.put(key, amount);
    }
}
