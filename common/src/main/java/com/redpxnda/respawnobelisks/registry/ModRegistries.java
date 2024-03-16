package com.redpxnda.respawnobelisks.registry;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
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
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

public class ModRegistries {
    private static final List<Supplier<ItemStack>> tabItems = new ArrayList<>();

    public static Identifier rl(String loc) {
        return new Identifier(MOD_ID, loc);
    }
    public static final Supplier<RegistrarManager> regs = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    private static final Registrar<Block> blocks = regs.get().get(RegistryKeys.BLOCK);
    private static final Registrar<ItemGroup> tabs = regs.get().get(RegistryKeys.ITEM_GROUP);
    private static final Registrar<Item> items = regs.get().get(RegistryKeys.ITEM);
    private static final Registrar<ParticleType<?>> particles = regs.get().get(RegistryKeys.PARTICLE_TYPE);
    private static final Registrar<Enchantment> enchants = regs.get().get(RegistryKeys.ENCHANTMENT);
    private static final Registrar<StatusEffect> effects = regs.get().get(RegistryKeys.STATUS_EFFECT);
    private static final Registrar<BlockEntityType<?>> blockEntities = regs.get().get(RegistryKeys.BLOCK_ENTITY_TYPE);
    private static final Registrar<StructureType<?>> structures = regs.get().get(RegistryKeys.STRUCTURE_TYPE);
    private static final Registrar<RecipeSerializer<?>> recipes = regs.get().get(RegistryKeys.RECIPE_SERIALIZER);

    public static RegistrySupplier<ParticleType<RuneCircleType.Options>> runeCircleParticle = particles.register(rl("rune_circle"), () -> new RuneCircleType(false));
    public static RegistrySupplier<DefaultParticleType> depleteRingParticle = particles.register(rl("deplete_ring"), () -> new DefaultParticleType(false));
    public static RegistrySupplier<DefaultParticleType> chargeIndicatorParticle = particles.register(rl("charge_indicator"), () -> new DefaultParticleType(false));

    public static RegistrySupplier<ItemGroup> tab = tabs.register(rl("tab"), () -> CreativeTabRegistry.create(b -> {
        b.displayName(Text.translatable("itemGroup.respawnobelisks.tab"));
        b.icon(() -> ModRegistries.respawnObeliskItem.get().getDefaultStack());
        b.entries((flagSet, out) -> tabItems.forEach(sup -> out.add(sup.get())));
    }));

    public static RegistrySupplier<Enchantment> obeliskbound = enchants.register(rl("obeliskbound"), ObeliskboundEnchantment::new);

    public static RegistrySupplier<Item> boundCompass = regItem("bound_compass", () -> new BoundCompassItem(new Item.Settings()));

    public static Identifier OBELISK_CORE_LOC = rl("obelisk_core");
    public static RegistrySupplier<Item> obeliskCore = regItem(OBELISK_CORE_LOC, () -> new CoreItem(new Item.Settings()
            .fireproof()
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

    public static RegistrySupplier<Block> respawnObelisk = blocks.register(rl("respawn_obelisk"), () -> new RespawnObeliskBlock(AbstractBlock.Settings
            .create()
            .sounds(BlockSoundGroup.STONE)
            .pistonBehavior(PistonBehavior.IGNORE)
            .mapColor(MapColor.GRAY)
            .nonOpaque()
            .strength(10, 3600.0F)
            .requiresTool(),
            (level, state, pos, blockEntity, player) -> RespawnObelisksConfig.INSTANCE.dimensions.isValidOverworld(level)
    ));

    public static RegistrySupplier<Item> respawnObeliskItem = regItem("respawn_obelisk", () -> new BlockItem(respawnObelisk.get(), new Item.Settings()
            .maxCount(1)
            .fireproof()
            .rarity(Rarity.UNCOMMON)
    ));

    public static RegistrySupplier<Block> netherRespawnObelisk = blocks.register(rl("respawn_obelisk_nether"), () -> new RespawnObeliskBlock(AbstractBlock.Settings
            .create()
            .sounds(BlockSoundGroup.STONE)
            .pistonBehavior(PistonBehavior.IGNORE)
            .mapColor(MapColor.RED)
            .nonOpaque()
            .strength(10, 3600.0F)
            .requiresTool(),
            (level, state, pos, blockEntity, player) -> RespawnObelisksConfig.INSTANCE.dimensions.isValidNether(level)
    ));

    public static RegistrySupplier<Item> netherRespawnObeliskItem = regItem("respawn_obelisk_nether", () -> new BlockItem(netherRespawnObelisk.get(), new Item.Settings()
            .maxCount(1)
            .fireproof()
            .rarity(Rarity.UNCOMMON)
    ));

    public static RegistrySupplier<Block> endRespawnObelisk = blocks.register(rl("respawn_obelisk_end"), () -> new RespawnObeliskBlock(AbstractBlock.Settings
            .create()
            .sounds(BlockSoundGroup.STONE)
            .pistonBehavior(PistonBehavior.IGNORE)
            .mapColor(MapColor.PURPLE)
            .nonOpaque()
            .strength(10, 3600.0F)
            .requiresTool(),
            (level, state, pos, blockEntity, player) -> RespawnObelisksConfig.INSTANCE.dimensions.isValidEnd(level)
    ));

    public static RegistrySupplier<Item> endRespawnObeliskItem = regItem("respawn_obelisk_end", () -> new BlockItem(endRespawnObelisk.get(), new Item.Settings()
            .maxCount(1)
            .fireproof()
            .rarity(Rarity.UNCOMMON)
    ));

    public static RegistrySupplier<Item> dormantObelisk = regItem("dormant_obelisk", () -> new Item(new Item.Settings()
            .rarity(Rarity.UNCOMMON)
    ));


    public static RegistrySupplier<Block> fakeRespawnAnchor = blocks.register(rl("fake_respawn_anchor"), () -> new FakeRespawnAnchorBlock(AbstractBlock.Settings
            .create()
            .sounds(BlockSoundGroup.STONE)
            .pistonBehavior(PistonBehavior.IGNORE)
            .strength(-1.0f, 3600000.0f)
            .dropsNothing()
    ));

    public static RegistrySupplier<BlockEntityType<RespawnObeliskBlockEntity>> ROBE = blockEntities.register(rl("respawn_obelisk"), () ->
            BlockEntityType.Builder.create(RespawnObeliskBlockEntity::new, respawnObelisk.get(), netherRespawnObelisk.get(), endRespawnObelisk.get()).build(null)
    );

    public static RegistrySupplier<StatusEffect> immortalityCurse = effects.register(rl("immortality_curse"), ImmortalityCurseEffect::new);

    public static RegistrySupplier<StructureType<NetherLandStructures>> netherStructureType = structures.register(rl("nether_land_structure"), () -> explicitStructureTypeTyping(NetherLandStructures.CODEC));

    public static RegistrySupplier<RecipeSerializer<?>> coreUpgrade = recipes.register(rl("core_upgrade"), CoreUpgradeRecipe.Serializer::new);
    public static RegistrySupplier<RecipeSerializer<?>> coreMerge = recipes.register(rl("core_merge"), CoreMergeRecipe.Serializer::new);

    public static EntityCriterion reviveCriterion = CriteriasAccessor.callRegister(new EntityCriterion(rl("revive_entity")));
    public static EmptyCriterion chargeCriterion = CriteriasAccessor.callRegister(new EmptyCriterion(rl("charge_obelisk")));
    public static EmptyCriterion respawnCriterion = CriteriasAccessor.callRegister(new EmptyCriterion(rl("respawn_at_obelisk")));
    public static EmptyCriterion keepItemsCriterion = CriteriasAccessor.callRegister(new EmptyCriterion(rl("items_restored")));
    public static EmptyCriterion kaboomCriterion = CriteriasAccessor.callRegister(new EmptyCriterion(rl("kaboom")));
    public static EmptyCriterion catalystCriterion = CriteriasAccessor.callRegister(new EmptyCriterion(rl("destruction_catalyst")));

    public static class EntityCriterion extends AbstractCriterion<EntityTriggerInstance> {
        private final Identifier id;

        public EntityCriterion(Identifier id) {
            this.id = id;
        }

        @Override
        protected EntityTriggerInstance conditionsFromJson(JsonObject jsonObject, LootContextPredicate composite, AdvancementEntityPredicateDeserializer deserializationContext) {
            LootContextPredicate entity = EntityPredicate.contextPredicateFromJson(jsonObject, "entity", deserializationContext);
            return new EntityTriggerInstance(id, composite, entity);
        }

        @Override
        public Identifier getId() {
            return id;
        }

        public void trigger(ServerPlayerEntity player, Entity entity) {
            this.trigger(player, i -> i.matches(player, entity));
        }
    }
    public static class EntityTriggerInstance extends AbstractCriterionConditions {
        private final LootContextPredicate entity;

        public EntityTriggerInstance(Identifier resourceLocation, LootContextPredicate composite, LootContextPredicate entity) {
            super(resourceLocation, composite);
            this.entity = entity;
        }

        public boolean matches(ServerPlayerEntity player, Entity entity) {
            return this.entity.test(EntityPredicate.createAdvancementEntityLootContext(player, entity));
        }
    }
    public static class EmptyCriterion extends AbstractCriterion<EmptyTriggerInstance> {
        public final Identifier id;

        public EmptyCriterion(Identifier id) {
            this.id = id;
        }

        @Override
        protected EmptyTriggerInstance conditionsFromJson(JsonObject jsonObject, LootContextPredicate composite, AdvancementEntityPredicateDeserializer deserializationContext) {
            return new EmptyTriggerInstance(id, composite);
        }

        public void trigger(ServerPlayerEntity player) {
            this.trigger(player, i -> true);
        }

        @Override
        public Identifier getId() {
            return id;
        }
    }
    public static class EmptyTriggerInstance extends AbstractCriterionConditions {
        public EmptyTriggerInstance(Identifier id, LootContextPredicate composite) {
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
    private static RegistrySupplier<Item> regItem(Identifier id, Supplier<Item> item, Supplier<ItemStack> tabItem) {
        tabItems.add(tabItem);
        return items.register(id, item);
    }
    private static RegistrySupplier<Item> regItem(Identifier id, Supplier<Item> item) {
        RegistrySupplier<Item> toReg = items.register(id, item);
        tabItems.add(() -> toReg.get().getDefaultStack());
        return toReg;
    }

    public static void init() {
        var classLoading = ModRegistries.class;
    }
}
