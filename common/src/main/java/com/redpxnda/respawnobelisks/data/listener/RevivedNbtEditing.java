package com.redpxnda.respawnobelisks.data.listener;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.redpxnda.nucleus.util.InterfaceDispatcher;
import com.redpxnda.nucleus.util.MiscUtil;
import com.redpxnda.respawnobelisks.RespawnObelisks;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

/**
 * system used to remove/adjust data in entity nbt
 */
public class RevivedNbtEditing extends JsonDataLoader {
    private static final Logger LOGGER = RespawnObelisks.getLogger();
    public static final Multimap<EntityType<?>, NbtAdjuster> typeNbtAdjusters = HashMultimap.create();
    public static final List<Pair<EntityPredicate, NbtAdjuster>> predicateNbtAdjusters = new ArrayList<>();
    public static final Map<String, EntityPredicate> builtinEntityPredicates = MiscUtil.initialize(new HashMap<>(), map -> {
        map.put("mobs", e -> e instanceof MobEntity);
        map.put("chested_horses", e -> e instanceof AbstractDonkeyEntity);
        map.put("horse_likes", e -> e instanceof AbstractHorseEntity);
    });
    public static final Map<String, NbtAdjusterCreator> adjusterCreators = MiscUtil.initialize(new HashMap<>(), map -> {
        map.put("remove", root -> {
            JsonObject obj = root.getAsJsonObject();

            JsonElement json = obj.get("target");
            JsonElement clearJson = obj.get("shouldClearList");
            boolean clear = clearJson != null && clearJson.getAsBoolean();

            if (json.isJsonArray())
                return new TagClearingAdjuster(json.getAsJsonArray().asList().stream().map(JsonElement::getAsString).toArray(String[]::new), clear);
            return new TagClearingAdjuster(json.getAsString().split("\\."), clear);
        });
    });
    private static final NbtAdjusterCreator dispatcher = InterfaceDispatcher.of(adjusterCreators, "type").dispatcher();

    public static void modify(NbtCompound tag, Entity entity) {
        typeNbtAdjusters.get(entity.getType()).forEach(a -> a.modify(tag));
        predicateNbtAdjusters.forEach(a -> {
            if (a.getFirst().isValid(entity)) a.getSecond().modify(tag);
        });
    }

    public RevivedNbtEditing() {
        super(RespawnObelisks.GSON, "revived_nbt");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> object, ResourceManager resourceManager, Profiler profilerFiller) {
        typeNbtAdjusters.clear();
        predicateNbtAdjusters.clear();

        object.forEach((rl, element) -> {
            if (!rl.getNamespace().equals("respawnobelisks")) return;
            if (element instanceof JsonObject obj) {
                List<EntityType<?>> entities = new ArrayList<>();
                List<EntityPredicate> predicates = new ArrayList<>();
                JsonElement entitiesRaw = obj.get("entities");

                if (entitiesRaw.isJsonPrimitive()) {
                    String entityStr = entitiesRaw.getAsString();
                    if (entityStr.equals("*"))
                        predicates.add(e -> true);
                    else if (entityStr.startsWith("$"))
                        predicates.add(builtinEntityPredicates.get(entityStr.substring(1)));
                    else
                        Registries.ENTITY_TYPE.getOrEmpty(new Identifier(entityStr)).ifPresent(entities::add);
                } else {
                    for (JsonElement emnt : entitiesRaw.getAsJsonArray()) {
                        Registries.ENTITY_TYPE.getOrEmpty(new Identifier(emnt.getAsString())).ifPresent(entities::add);
                    }
                }

                List<NbtAdjuster> adjusters = new ArrayList<>();
                JsonElement adjustersJson = obj.get("adjusters");
                if (adjustersJson.isJsonArray())
                    adjustersJson.getAsJsonArray().forEach(e -> adjusters.add(dispatcher.createAdjuster(e)));
                else
                    adjusters.add(dispatcher.createAdjuster(adjustersJson));

                predicates.forEach(p -> adjusters.forEach(adjuster -> predicateNbtAdjusters.add(Pair.of(p, adjuster))));
                entities.forEach(type -> adjusters.forEach(adjuster -> typeNbtAdjusters.put(type, adjuster)));
            }
        });
    }

    public interface EntityPredicate {
        boolean isValid(Entity entity);
    }

    public interface NbtAdjusterCreator {
        NbtAdjuster createAdjuster(JsonElement element);
    }

    public interface NbtAdjuster {
        void modify(NbtCompound storedEntityData);
    }

    public record TagClearingAdjuster(String[] targets, boolean clearList) implements NbtAdjuster {
        @Override
        public void modify(NbtCompound storedEntityData) {
            NbtCompound currentTarget = storedEntityData;
            for (int i = 0; i < targets.length; i++) {
                String current = targets[i];
                boolean isLast = i == targets.length-1;
                if (isLast) {
                    NbtElement tag = currentTarget.get(current);
                    if (clearList && tag instanceof AbstractNbtList<?> cl) cl.clear();
                    else currentTarget.remove(current);
                    break;
                } else currentTarget = currentTarget.getCompound(current);
            }
        }
    }
}
