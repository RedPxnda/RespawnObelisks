package com.redpxnda.respawnobelisks.data.listener;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

public class ObeliskInteraction {
    public static Map<GameEvent, Map<ResourceLocation, ObeliskInteraction>> EVENT_INTERACTIONS = new HashMap<>();
    public static List<ObeliskInteraction> RIGHT_CLICK_INTERACTIONS = new ArrayList<>();
    public static Multimap<Injection, ObeliskInteraction> RESPAWN_INTERACTIONS = HashMultimap.create();

    public static ObeliskInteraction DEFAULT_CHARGING = ofClick(new ResourceLocation(MOD_ID, "default_charging"), (player, stack, be) -> {
        if (!RespawnObelisksConfig.INSTANCE.radiance.chargingItems.containsKey(stack.getItem())) return false; // If the held item isn't in the config, don't do anything

        double charge = RespawnObelisksConfig.INSTANCE.radiance.chargingItems.get(stack.getItem()); // getting charge value of item
        double currentCharge = be.getCharge(player); // getting obelisk's charge level

        if (currentCharge + charge > be.getMaxCharge(player) || (currentCharge <= 0 && charge < 0)) return false; // don't allow when charge goes too high

        if (player != null) RespawnObelisksConfig.INSTANCE.radiance.chargingItems.keySet().forEach(i -> player.getCooldowns().addCooldown(i, 30)); // adding cooldown

        be.chargeAndAnimate(player, charge); // method name says it all

        if (player == null || !player.getAbilities().instabuild) stack.shrink(1); // if not in creative, remove the item
        return true;
    });
    public static ObeliskInteraction INFINITE_CHARGE = ofRespawn(new ResourceLocation(MOD_ID, "infinite_charge"), Injection.START, ((player, be, manager) -> {
        if (!be.hasLevel()) return;
        if (RespawnObelisksConfig.INSTANCE.radiance.obeliskGetsInfiniteRadiance(be.getLevel(), be.getBlockPos()))
            manager.cost = 0;
    }));
    public static ObeliskInteraction TELEPORT = new ObeliskInteraction(new ResourceLocation(MOD_ID, "teleportation"));
    public static ObeliskInteraction REVIVE = new ObeliskInteraction(new ResourceLocation(MOD_ID, "revival"));
    public static ObeliskInteraction PROTECT = new ObeliskInteraction(new ResourceLocation(MOD_ID, "player_protection"));
    public static ObeliskInteraction SAVE_INV = new ObeliskInteraction(new ResourceLocation(MOD_ID, "item_keeping"));
    public static ObeliskInteraction RADIANT_FLAME_FUELING = ofClick(new ResourceLocation(MOD_ID, "radiant_flame_fueling"), (player, stack, be) -> {
        if (player == null || !stack.is(ModRegistries.radiantLantern.get()) || CoreUtils.getCharge(stack.getOrCreateTag()) > 0) return false;
        double charge = be.getCharge(player);
        charge = Math.min(charge, RespawnObelisksConfig.INSTANCE.radiantFlame.maxLanternRadiance);
        double reducedCharge = charge*RespawnObelisksConfig.INSTANCE.radiantFlame.radianceEfficiency;
        if (reducedCharge < RespawnObelisksConfig.INSTANCE.radiance.respawnCost) return false; // must have charge for at least 1 respawn
        be.chargeAndAnimate(player, -charge);

        ItemStack copy = stack.copy(); // only charge 1 item
        copy.setCount(1);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        CoreUtils.setCharge(copy.getOrCreateTag(), reducedCharge);
        player.getInventory().placeItemBackInInventory(copy);

        return true;
    });

    public final ResourceLocation id;
    public final BiFunction<RespawnObeliskBlockEntity, GameEvent.ListenerInfo, Boolean> eventHandler;
    public final TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Boolean> clickHandler;
    public final TriConsumer<Player, RespawnObeliskBlockEntity, Manager> respawnHandler;

    // For GameEvents
    private ObeliskInteraction(GameEvent event, ResourceLocation id, BiFunction<RespawnObeliskBlockEntity, GameEvent.ListenerInfo, Boolean> handler) {
        this.id = id;
        this.eventHandler = handler;
        this.clickHandler = (p, i, b) -> false;
        this.respawnHandler = (p, be, m) -> {};
        if (!EVENT_INTERACTIONS.containsKey(event)) EVENT_INTERACTIONS.put(event, new HashMap<>());
        EVENT_INTERACTIONS.get(event).put(id, this);
    }

    // For Right-Clicking
    private ObeliskInteraction(ResourceLocation id, TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Boolean> handler) {
        this.id = id;
        this.eventHandler = (be, message) -> false;
        this.clickHandler = handler;
        this.respawnHandler = (p, be, m) -> {};
        RIGHT_CLICK_INTERACTIONS.add(this);
    }

    // For Respawning
    private ObeliskInteraction(ResourceLocation id, Injection injection, TriConsumer<Player, RespawnObeliskBlockEntity, Manager> handler) {
        this.id = id;
        this.eventHandler = (be, message) -> false;
        this.clickHandler = (p, i, be) -> false;
        this.respawnHandler = handler;
        RESPAWN_INTERACTIONS.put(injection, this);
    }

    // For hardcoded use
    public ObeliskInteraction(ResourceLocation id) {
        this.id = id;
        this.eventHandler = (be, message) -> false;
        this.clickHandler = (p, i, be) -> false;
        this.respawnHandler = (p, be, m) -> {};
    }

    public static ObeliskInteraction ofEvent(ResourceLocation id, GameEvent event, BiFunction<RespawnObeliskBlockEntity, GameEvent.ListenerInfo, Boolean> handler) {
        return new ObeliskInteraction(event, id, handler);
    }
    public static ObeliskInteraction ofClick(ResourceLocation id, TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Boolean> handler) {
        return new ObeliskInteraction(id, handler);
    }
    public static ObeliskInteraction ofRespawn(ResourceLocation id, Injection injection, TriConsumer<Player, RespawnObeliskBlockEntity, Manager> handler) {
        return new ObeliskInteraction(id, injection, handler);
    }

    /**
     * {@link Injection} is an enum(group of constants) made for the sole purpose of managing respawn interaction injection points.
     * Modifying respawn cost is a common goal of respawn interactions, but injecting at the end can often lead to unwanted behavior. So, inject at the start.
     * Modifying respawn position is also a common goal, except you can't exactly modify that before it's calculated. So, inject at the end.
     * <p>
     * WARNING: When injecting at the start, respawn position is unavailable. Attempting to fetch it will result in errors.
     */
    public enum Injection {
        START,
        END
    }

    public static class Manager {
        public boolean curseForced;
        public boolean shouldConsumeCost;
        public double cost;
        public Vec3 spawnLoc;

        public Manager(boolean curseForced, boolean shouldConsumeCost, double cost, Vec3 spawnLoc) {
            this.curseForced = curseForced;
            this.shouldConsumeCost = shouldConsumeCost;
            this.cost = cost;
            this.spawnLoc = spawnLoc;
        }

        public double getCost() {
            return cost;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }

        public Vec3 getSpawnLoc() {
            return spawnLoc;
        }

        public void setSpawnLoc(Vec3 spawnLoc) {
            this.spawnLoc = spawnLoc;
        }
    }
}
