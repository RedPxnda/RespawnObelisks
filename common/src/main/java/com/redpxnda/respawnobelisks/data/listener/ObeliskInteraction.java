package com.redpxnda.respawnobelisks.data.listener;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

public class ObeliskInteraction {
    public static Map<GameEvent, Map<Identifier, ObeliskInteraction>> EVENT_INTERACTIONS = new HashMap<>();
    public static List<ObeliskInteraction> RIGHT_CLICK_INTERACTIONS = new ArrayList<>();
    public static Multimap<Injection, ObeliskInteraction> RESPAWN_INTERACTIONS = HashMultimap.create();

    public static ObeliskInteraction DEFAULT_CHARGING = ofClick(new Identifier(MOD_ID, "default_charging"), (player, stack, be) -> {
        if (!RespawnObelisksConfig.INSTANCE.radiance.chargingItems.containsKey(stack.getItem())) return false; // If the held item isn't in the config, don't do anything

        double charge = RespawnObelisksConfig.INSTANCE.radiance.chargingItems.get(stack.getItem()); // getting charge value of item
        double currentCharge = be.getCharge(player); // getting obelisk's charge level

        if (currentCharge + charge > be.getMaxCharge(player) || (currentCharge <= 0 && charge < 0)) return false; // don't allow when charge goes too high

        RespawnObelisksConfig.INSTANCE.radiance.chargingItems.keySet().forEach(i -> player.getItemCooldownManager().set(i, 30)); // adding cooldown

        be.chargeAndAnimate(player, charge); // method name says it all

        if (!player.getAbilities().creativeMode) player.getMainHandStack().decrement(1); // if not in creative, remove the item
        return true;
    });
    public static ObeliskInteraction INFINITE_CHARGE = ofRespawn(new Identifier(MOD_ID, "infinite_charge"), Injection.START, ((player, be, manager) -> {
        if (!be.hasWorld()) return;
        if (RespawnObelisksConfig.INSTANCE.radiance.providesInfiniteRadiance(be.getWorld().getBlockState(be.getPos().down())))
            manager.cost = 0;
    }));
    public static ObeliskInteraction TELEPORT = new ObeliskInteraction(new Identifier(MOD_ID, "teleportation"));
    public static ObeliskInteraction REVIVE = new ObeliskInteraction(new Identifier(MOD_ID, "revival"));
    public static ObeliskInteraction PROTECT = new ObeliskInteraction(new Identifier(MOD_ID, "player_protection"));
    public static ObeliskInteraction SAVE_INV = new ObeliskInteraction(new Identifier(MOD_ID, "item_keeping"));

    public final Identifier id;
    public final BiFunction<RespawnObeliskBlockEntity, GameEvent.Message, Boolean> eventHandler;
    public final TriFunction<PlayerEntity, ItemStack, RespawnObeliskBlockEntity, Boolean> clickHandler;
    public final TriConsumer<PlayerEntity, RespawnObeliskBlockEntity, Manager> respawnHandler;

    // For GameEvents
    private ObeliskInteraction(GameEvent event, Identifier id, BiFunction<RespawnObeliskBlockEntity, GameEvent.Message, Boolean> handler) {
        this.id = id;
        this.eventHandler = handler;
        this.clickHandler = (p, i, b) -> false;
        this.respawnHandler = (p, be, m) -> {};
        if (!EVENT_INTERACTIONS.containsKey(event)) EVENT_INTERACTIONS.put(event, new HashMap<>());
        EVENT_INTERACTIONS.get(event).put(id, this);
    }

    // For Right-Clicking
    private ObeliskInteraction(Identifier id, TriFunction<PlayerEntity, ItemStack, RespawnObeliskBlockEntity, Boolean> handler) {
        this.id = id;
        this.eventHandler = (be, message) -> false;
        this.clickHandler = handler;
        this.respawnHandler = (p, be, m) -> {};
        RIGHT_CLICK_INTERACTIONS.add(this);
    }

    // For Respawning
    private ObeliskInteraction(Identifier id, Injection injection, TriConsumer<PlayerEntity, RespawnObeliskBlockEntity, Manager> handler) {
        this.id = id;
        this.eventHandler = (be, message) -> false;
        this.clickHandler = (p, i, be) -> false;
        this.respawnHandler = handler;
        RESPAWN_INTERACTIONS.put(injection, this);
    }

    // For hardcoded use
    public ObeliskInteraction(Identifier id) {
        this.id = id;
        this.eventHandler = (be, message) -> false;
        this.clickHandler = (p, i, be) -> false;
        this.respawnHandler = (p, be, m) -> {};
    }

    public static ObeliskInteraction ofEvent(Identifier id, GameEvent event, BiFunction<RespawnObeliskBlockEntity, GameEvent.Message, Boolean> handler) {
        return new ObeliskInteraction(event, id, handler);
    }
    public static ObeliskInteraction ofClick(Identifier id, TriFunction<PlayerEntity, ItemStack, RespawnObeliskBlockEntity, Boolean> handler) {
        return new ObeliskInteraction(id, handler);
    }
    public static ObeliskInteraction ofRespawn(Identifier id, Injection injection, TriConsumer<PlayerEntity, RespawnObeliskBlockEntity, Manager> handler) {
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
        public double cost;
        public Vec3d spawnLoc;

        public Manager(double cost, Vec3d spawnLoc) {
            this.cost = cost;
            this.spawnLoc = spawnLoc;
        }

        public double getCost() {
            return cost;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }

        public Vec3d getSpawnLoc() {
            return spawnLoc;
        }

        public void setSpawnLoc(Vec3d spawnLoc) {
            this.spawnLoc = spawnLoc;
        }
    }
}
