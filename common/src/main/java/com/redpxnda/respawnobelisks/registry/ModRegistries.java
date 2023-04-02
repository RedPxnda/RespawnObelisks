package com.redpxnda.respawnobelisks.registry;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.redpxnda.respawnobelisks.event.ClientEvents;
import com.redpxnda.respawnobelisks.mixin.SimpleParticleTypeAccessor;
import com.redpxnda.respawnobelisks.registry.block.FakeRespawnAnchorBlock;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.registry.effect.ImmortalityCurseEffect;
import com.redpxnda.respawnobelisks.registry.item.BoundCompassItem;
import com.redpxnda.respawnobelisks.registry.structure.NetherLandStructures;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.Registries;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.material.Material;

import java.util.function.Supplier;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

public class ModRegistries {
    public static final Supplier<Registries> REGISTRIES = Suppliers.memoize(() -> Registries.get(MOD_ID));

    public static Registrar<Block> BLOCKS = REGISTRIES.get().get(Registry.BLOCK_REGISTRY);
    public static Registrar<Item> ITEMS = REGISTRIES.get().get(Registry.ITEM_REGISTRY);
    public static Registrar<ParticleType<?>> PARTICLE_TYPES = REGISTRIES.get().get(Registry.PARTICLE_TYPE_REGISTRY);
    public static Registrar<MobEffect> EFFECTS = REGISTRIES.get().get(Registry.MOB_EFFECT_REGISTRY);
    public static Registrar<BlockEntityType<?>> BLOCK_ENTITIES = REGISTRIES.get().get(Registry.BLOCK_ENTITY_TYPE_REGISTRY);
    public static Registrar<StructureType<?>> STRUCTURES = REGISTRIES.get().get(Registry.STRUCTURE_TYPE_REGISTRY);

    public static RegistrySupplier<ParticleType<SimpleParticleType>> RUNE_CIRCLE_PARTICLE = PARTICLE_TYPES.register(new ResourceLocation(MOD_ID, "rune_circle"), () -> SimpleParticleTypeAccessor.newTarget(false));

    public static RegistrySupplier<Item> BOUND_COMPASS = ITEMS.register(new ResourceLocation(MOD_ID, "bound_compass"), () -> new BoundCompassItem(new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
    ));

    public static RegistrySupplier<Item> OBELISK_CORE = ITEMS.register(new ResourceLocation(MOD_ID, "obelisk_core"), () -> new Item(new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));

    public static RegistrySupplier<Item> OBELISK_CORE_NETHER = ITEMS.register(new ResourceLocation(MOD_ID, "obelisk_core_nether"), () -> new Item(new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));

    public static RegistrySupplier<Item> OBELISK_CORE_END = ITEMS.register(new ResourceLocation(MOD_ID, "obelisk_core_end"), () -> new Item(new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));

    public static RegistrySupplier<Block> RESPAWN_OBELISK_BLOCK = BLOCKS.register(new ResourceLocation(MOD_ID, "respawn_obelisk"), () -> new RespawnObeliskBlock(BlockBehaviour.Properties
            .of(Material.STONE)
            .noOcclusion()
            .strength(2, 3600.0F)
            .requiresCorrectToolForDrops(),
            Either.left(Level.OVERWORLD),
            OBELISK_CORE
    ));

    public static RegistrySupplier<Item> RESPAWN_OBELISK_ITEM = ITEMS.register(new ResourceLocation(MOD_ID, "respawn_obelisk"), () -> new BlockItem(RESPAWN_OBELISK_BLOCK.get(), new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(1)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));

    public static RegistrySupplier<Block> RESPAWN_OBELISK_BLOCK_NETHER = BLOCKS.register(new ResourceLocation(MOD_ID, "respawn_obelisk_nether"), () -> new RespawnObeliskBlock(BlockBehaviour.Properties
            .of(Material.STONE)
            .noOcclusion()
            .strength(2, 3600.0F)
            .requiresCorrectToolForDrops(),
            Either.left(Level.NETHER),
            OBELISK_CORE_NETHER
    ));

    public static RegistrySupplier<Item> RESPAWN_OBELISK_ITEM_NETHER = ITEMS.register(new ResourceLocation(MOD_ID, "respawn_obelisk_nether"), () -> new BlockItem(RESPAWN_OBELISK_BLOCK_NETHER.get(), new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(1)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));

    public static RegistrySupplier<Block> RESPAWN_OBELISK_BLOCK_END = BLOCKS.register(new ResourceLocation(MOD_ID, "respawn_obelisk_end"), () -> new RespawnObeliskBlock(BlockBehaviour.Properties
            .of(Material.STONE)
            .noOcclusion()
            .strength(2, 3600.0F)
            .requiresCorrectToolForDrops(),
            Either.left(Level.END),
            OBELISK_CORE_END
    ));

    public static RegistrySupplier<Item> RESPAWN_OBELISK_ITEM_END = ITEMS.register(new ResourceLocation(MOD_ID, "respawn_obelisk_end"), () -> new BlockItem(RESPAWN_OBELISK_BLOCK_END.get(), new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(1)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));

    public static RegistrySupplier<Item> DORMANT_OBELISK = ITEMS.register(new ResourceLocation(MOD_ID, "dormant_obelisk"), () -> new Item(new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .rarity(Rarity.UNCOMMON)
    ));


    public static RegistrySupplier<Block> FAKE_ANCHOR_BLOCK = BLOCKS.register(new ResourceLocation(MOD_ID, "fake_respawn_anchor"), () -> new FakeRespawnAnchorBlock(BlockBehaviour.Properties
            .of(Material.STONE)
            .strength(-1, 3600000.0F)
            .noLootTable()
    ));

    public static RegistrySupplier<BlockEntityType<RespawnObeliskBlockEntity>> RESPAWN_OBELISK_BE = BLOCK_ENTITIES.register(new ResourceLocation(MOD_ID, "respawn_obelisk"), () ->
            BlockEntityType.Builder.of(RespawnObeliskBlockEntity::new, RESPAWN_OBELISK_BLOCK.get(), RESPAWN_OBELISK_BLOCK_NETHER.get(), RESPAWN_OBELISK_BLOCK_END.get()).build(null)
    );

    // effects
    public static RegistrySupplier<MobEffect> IMMORTALITY_CURSE = EFFECTS.register(new ResourceLocation(MOD_ID, "immortality_curse"), ImmortalityCurseEffect::new);

    public static RegistrySupplier<StructureType<NetherLandStructures>> NETHER_STRUCTURES = STRUCTURES.register(new ResourceLocation(MOD_ID, "nether_land_structure"), () -> explicitStructureTypeTyping(NetherLandStructures.CODEC));

    private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(Codec<T> structureCodec) {
        return () -> structureCodec;
    }

    public static void init() {
        var classLoading = ModRegistries.class;
    }
}
