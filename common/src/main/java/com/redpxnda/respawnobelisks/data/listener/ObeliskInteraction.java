package com.redpxnda.respawnobelisks.data.listener;

import com.redpxnda.nucleus.datapack.references.GameEventReference;
import com.redpxnda.nucleus.datapack.references.entity.PlayerReference;
import com.redpxnda.nucleus.datapack.references.item.ItemStackReference;
import com.redpxnda.respawnobelisks.config.ChargeConfig;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

public class ObeliskInteraction {
    public static Map<GameEvent, Map<ResourceLocation, ObeliskInteraction>> INTERACTIONS = new HashMap<>();
    public static List<ObeliskInteraction> RIGHT_CLICK_INTERACTIONS = new ArrayList<>();

    public static ObeliskInteraction DEFAULT_CHARGING = create(new ResourceLocation(MOD_ID, "default_charging"), (player, stack, be) -> {
        if (!ChargeConfig.getChargeItems().containsKey(stack.getItem())) return false; // If the held item isn't in the config, don't do anything

        double charge = ChargeConfig.getChargeItems().get(stack.getItem()); // getting charge value of item
        double currentCharge = be.getCharge(player); // getting obelisk's charge level

        if (currentCharge + charge > be.getMaxCharge(player)) return false; // don't allow when charge goes too high

        ChargeConfig.getChargeItems().keySet().forEach(i -> player.getCooldowns().addCooldown(i, 30)); // adding cooldown

        be.chargeAndAnimate(player, charge); // method name says it all

        if (!player.getAbilities().instabuild) player.getMainHandItem().shrink(1); // if not in creative, remove the item
        return true;
    });

    public final ResourceLocation id;
    public final BiFunction<RespawnObeliskBlockEntity, GameEvent.Message, Boolean> handler;
    public final TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Boolean> clickHandler;
    private ObeliskInteraction(GameEvent event, ResourceLocation id, BiFunction<RespawnObeliskBlockEntity, GameEvent.Message, Boolean> handler) {
        this.id = id;
        this.handler = handler;
        this.clickHandler = (p, i, b) -> false;
        if (!INTERACTIONS.containsKey(event)) INTERACTIONS.put(event, new HashMap<>());
        INTERACTIONS.get(event).put(id, this);
    }

    private ObeliskInteraction(ResourceLocation id, TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Boolean> handler) {
        this.id = id;
        this.handler = (be, message) -> false;
        this.clickHandler = handler;
        RIGHT_CLICK_INTERACTIONS.add(this);
    }

    public static ObeliskInteraction create(ResourceLocation id, @Nullable GameEvent event, BiFunction<RespawnObeliskBlockEntity, GameEvent.Message, Boolean> handler) {
        return new ObeliskInteraction(event, id, handler);
    }
    public static ObeliskInteraction create(String id, String event, LuaFunction handler) {
        return new ObeliskInteraction(Registry.GAME_EVENT.get(new ResourceLocation(event)), new ResourceLocation(id), (be, message) -> handler.call(CoerceJavaToLua.coerce(new ROBEReference(be)), CoerceJavaToLua.coerce(new GameEventReference.Message(message))).toboolean());
    }
    public static ObeliskInteraction create(ResourceLocation id, TriFunction<Player, ItemStack, RespawnObeliskBlockEntity, Boolean> handler) {
        return new ObeliskInteraction(id, handler);
    }
    public static ObeliskInteraction create(String id, LuaFunction handler) {
        return new ObeliskInteraction(new ResourceLocation(id), (player, item, be) -> handler.call(CoerceJavaToLua.coerce(new PlayerReference(player)), CoerceJavaToLua.coerce(new ItemStackReference(item)), CoerceJavaToLua.coerce(new ROBEReference(be))).toboolean());
    }
}
