package com.redpxnda.respawnobelisks.registry;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.redpxnda.respawnobelisks.data.recipe.CoreMergeRecipe;
import com.redpxnda.respawnobelisks.data.recipe.CoreUpgradeRecipe;
import com.redpxnda.respawnobelisks.mixin.CriteriasAccessor;
import com.redpxnda.respawnobelisks.registry.block.FakeRespawnAnchorBlock;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.registry.effect.ImmortalityCurseEffect;
import com.redpxnda.respawnobelisks.registry.enchantment.ObeliskboundEnchantment;
import com.redpxnda.respawnobelisks.registry.item.BoundCompassItem;
import com.redpxnda.respawnobelisks.registry.item.CoreItem;
import com.redpxnda.respawnobelisks.registry.particle.RuneCircleType;
import com.redpxnda.respawnobelisks.registry.structure.NetherLandStructures;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

public class ModRegistries {
    private static final List<Supplier<ItemStack>> tabItems = new ArrayList<>();

    public static ResourceLocation rl(String loc) {
        return new ResourceLocation(MOD_ID, loc);
    }
    public static final Supplier<RegistrarManager> regs = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    private static final Registrar<Block> blocks = regs.get().get(Registries.BLOCK);
    private static final Registrar<CreativeModeTab> tabs = regs.get().get(Registries.CREATIVE_MODE_TAB);
    private static final Registrar<Item> items = regs.get().get(Registries.ITEM);
    private static final Registrar<ParticleType<?>> particles = regs.get().get(Registries.PARTICLE_TYPE);
    private static final Registrar<Enchantment> enchants = regs.get().get(Registries.ENCHANTMENT);
    private static final Registrar<MobEffect> effects = regs.get().get(Registries.MOB_EFFECT);
    private static final Registrar<BlockEntityType<?>> blockEntities = regs.get().get(Registries.BLOCK_ENTITY_TYPE);
    private static final Registrar<StructureType<?>> structures = regs.get().get(Registries.STRUCTURE_TYPE);
    private static final Registrar<RecipeSerializer<?>> recipes = regs.get().get(Registries.RECIPE_SERIALIZER);

    public static RegistrySupplier<ParticleType<RuneCircleType.Options>> runeCircleParticle = particles.register(rl("rune_circle"), () -> new RuneCircleType(false));
    public static RegistrySupplier<SimpleParticleType> depleteRingParticle = particles.register(rl("deplete_ring"), () -> new SimpleParticleType(false));
    public static RegistrySupplier<SimpleParticleType> chargeIndicatorParticle = particles.register(rl("charge_indicator"), () -> new SimpleParticleType(false));

    public static RegistrySupplier<CreativeModeTab> tab = tabs.register(rl("tab"), () -> CreativeTabRegistry.create(b -> {
        b.title(Component.translatable("itemGroup.respawnobelisks.tab"));
        b.icon(() -> ModRegistries.respawnObeliskItem.get().getDefaultInstance());
        b.displayItems((flagSet, out) -> tabItems.forEach(sup -> out.accept(sup.get())));
    }));

    public static RegistrySupplier<Enchantment> obeliskbound = enchants.register(rl("obeliskbound"), ObeliskboundEnchantment::new);

    public static RegistrySupplier<Item> boundCompass = regItem("bound_compass", () -> new BoundCompassItem(new Item.Properties()));

    public static ResourceLocation OBELISK_CORE_LOC = rl("obelisk_core");
    public static RegistrySupplier<Item> obeliskCore = regItem(OBELISK_CORE_LOC, () -> new CoreItem(new Item.Properties()
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ), () -> CoreItem.createTabItem(ModRegistries.obeliskCore.get()));

//    public static RegistrySupplier<Item> OBELISK_CORE_NETHER = ITEMS.register(loc("obelisk_core_nether"), () -> new CoreItem(new Item.Properties()
//            .tab(CreativeModeTab.TAB_MISC)
//            .fireResistant()
//            .rarity(Rarity.UNCOMMON)
//    ));
//
//    public static RegistrySupplier<Item> OBELISK_CORE_END = ITEMS.register(loc("obelisk_core_end"), () -> new CoreItem(new Item.Properties()
//            .tab(CreativeModeTab.TAB_MISC)
//            .fireResistant()
//            .rarity(Rarity.UNCOMMON)
//    ));

    public static RegistrySupplier<Block> respawnObelisk = blocks.register(rl("respawn_obelisk"), () -> new RespawnObeliskBlock(BlockBehaviour.Properties
            .of()
            .sound(SoundType.STONE)
            .pushReaction(PushReaction.IGNORE)
            .mapColor(MapColor.COLOR_GRAY)
            .noOcclusion()
            .strength(10, 3600.0F)
            .requiresCorrectToolForDrops(),
            Level.OVERWORLD
    ));

    public static RegistrySupplier<Item> respawnObeliskItem = regItem("respawn_obelisk", () -> new BlockItem(respawnObelisk.get(), new Item.Properties()
            .stacksTo(1)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));

    public static RegistrySupplier<Block> netherRespawnObelisk = blocks.register(rl("respawn_obelisk_nether"), () -> new RespawnObeliskBlock(BlockBehaviour.Properties
            .of()
            .sound(SoundType.STONE)
            .pushReaction(PushReaction.IGNORE)
            .mapColor(MapColor.COLOR_RED)
            .noOcclusion()
            .strength(10, 3600.0F)
            .requiresCorrectToolForDrops(),
            Level.NETHER
    ));

    public static RegistrySupplier<Item> netherRespawnObeliskItem = regItem("respawn_obelisk_nether", () -> new BlockItem(netherRespawnObelisk.get(), new Item.Properties()
            .stacksTo(1)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));

    public static RegistrySupplier<Block> endRespawnObelisk = blocks.register(rl("respawn_obelisk_end"), () -> new RespawnObeliskBlock(BlockBehaviour.Properties
            .of()
            .sound(SoundType.STONE)
            .pushReaction(PushReaction.IGNORE)
            .mapColor(MapColor.COLOR_PURPLE)
            .noOcclusion()
            .strength(10, 3600.0F)
            .requiresCorrectToolForDrops(),
            Level.END
    ));

    public static RegistrySupplier<Item> endRespawnObeliskItem = regItem("respawn_obelisk_end", () -> new BlockItem(endRespawnObelisk.get(), new Item.Properties()
            .stacksTo(1)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));

    public static RegistrySupplier<Item> dormantObelisk = regItem("dormant_obelisk", () -> new Item(new Item.Properties()
            .rarity(Rarity.UNCOMMON)
    ));


    public static RegistrySupplier<Block> fakeRespawnAnchor = blocks.register(rl("fake_respawn_anchor"), () -> new FakeRespawnAnchorBlock(BlockBehaviour.Properties
            .of()
            .sound(SoundType.STONE)
            .pushReaction(PushReaction.IGNORE)
            .strength(-1.0f, 3600000.0f)
            .noLootTable()
    ));

    public static RegistrySupplier<BlockEntityType<RespawnObeliskBlockEntity>> ROBE = blockEntities.register(rl("respawn_obelisk"), () ->
            BlockEntityType.Builder.of(RespawnObeliskBlockEntity::new, respawnObelisk.get(), netherRespawnObelisk.get(), endRespawnObelisk.get()).build(null)
    );

    public static RegistrySupplier<MobEffect> immortalityCurse = effects.register(rl("immortality_curse"), ImmortalityCurseEffect::new);

    public static RegistrySupplier<StructureType<NetherLandStructures>> netherStructureType = structures.register(rl("nether_land_structure"), () -> explicitStructureTypeTyping(NetherLandStructures.CODEC));

    public static RegistrySupplier<RecipeSerializer<?>> coreUpgrade = recipes.register(rl("core_upgrade"), CoreUpgradeRecipe.Serializer::new);
    public static RegistrySupplier<RecipeSerializer<?>> coreMerge = recipes.register(rl("core_merge"), CoreMergeRecipe.Serializer::new);

    public static EntityCriterion reviveCriterion = CriteriasAccessor.callRegister(new EntityCriterion(rl("revive_entity")));
    public static EmptyCriterion chargeCriterion = CriteriasAccessor.callRegister(new EmptyCriterion(rl("charge_obelisk")));
    public static EmptyCriterion respawnCriterion = CriteriasAccessor.callRegister(new EmptyCriterion(rl("respawn_at_obelisk")));
    public static EmptyCriterion keepItemsCriterion = CriteriasAccessor.callRegister(new EmptyCriterion(rl("items_restored")));
    public static EmptyCriterion kaboomCriterion = CriteriasAccessor.callRegister(new EmptyCriterion(rl("kaboom")));
    public static EmptyCriterion catalystCriterion = CriteriasAccessor.callRegister(new EmptyCriterion(rl("destruction_catalyst")));

    public static class EntityCriterion extends SimpleCriterionTrigger<EntityTriggerInstance> {
        private final ResourceLocation id;

        public EntityCriterion(ResourceLocation id) {
            this.id = id;
        }

        @Override
        protected EntityTriggerInstance createInstance(JsonObject jsonObject, ContextAwarePredicate composite, DeserializationContext deserializationContext) {
            ContextAwarePredicate entity = EntityPredicate.fromJson(jsonObject, "entity", deserializationContext);
            return new EntityTriggerInstance(id, composite, entity);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        public void trigger(ServerPlayer player, Entity entity) {
            this.trigger(player, i -> i.matches(player, entity));
        }
    }
    public static class EntityTriggerInstance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate entity;

        public EntityTriggerInstance(ResourceLocation resourceLocation, ContextAwarePredicate composite, ContextAwarePredicate entity) {
            super(resourceLocation, composite);
            this.entity = entity;
        }

        public boolean matches(ServerPlayer player, Entity entity) {
            return this.entity.matches(EntityPredicate.createContext(player, entity));
        }
    }
    public static class EmptyCriterion extends SimpleCriterionTrigger<EmptyTriggerInstance> {
        public final ResourceLocation id;

        public EmptyCriterion(ResourceLocation id) {
            this.id = id;
        }

        @Override
        protected ModRegistries.EmptyTriggerInstance createInstance(JsonObject jsonObject, ContextAwarePredicate composite, DeserializationContext deserializationContext) {
            return new ModRegistries.EmptyTriggerInstance(id, composite);
        }

        public void trigger(ServerPlayer player) {
            this.trigger(player, i -> true);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }
    }
    public static class EmptyTriggerInstance extends AbstractCriterionTriggerInstance {
        public EmptyTriggerInstance(ResourceLocation id, ContextAwarePredicate composite) {
            super(id, composite);
        }
    }

    private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(Codec<T> structureCodec) {
        return () -> structureCodec;
    }

    private static RegistrySupplier<Item> regItem(String id, Supplier<Item> item, Supplier<ItemStack> tabItem) {
        return regItem(rl(id), item, tabItem);
    }
    private static RegistrySupplier<Item> regItem(String id, Supplier<Item> item) {
        return regItem(rl(id), item);
    }
    private static RegistrySupplier<Item> regItem(ResourceLocation id, Supplier<Item> item, Supplier<ItemStack> tabItem) {
        tabItems.add(tabItem);
        return items.register(id, item);
    }
    private static RegistrySupplier<Item> regItem(ResourceLocation id, Supplier<Item> item) {
        RegistrySupplier<Item> toReg = items.register(id, item);
        tabItems.add(() -> toReg.get().getDefaultInstance());
        return toReg;
    }

    public static void init() {
        var classLoading = ModRegistries.class;
    }
}
