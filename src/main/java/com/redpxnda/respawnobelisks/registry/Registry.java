package com.redpxnda.respawnobelisks.registry;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.redpxnda.respawnobelisks.registry.blocks.FakeRespawnAnchorBlock;
import com.redpxnda.respawnobelisks.registry.blocks.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.blocks.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.registry.effects.ImmortalityCurseEffect;
import com.redpxnda.respawnobelisks.structure.NetherLandStructures;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MODID;

public class Registry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
    public static final RegistryObject<MobEffect> IMMORTALITY_CURSE = MOB_EFFECTS.register("curse_of_immortality", ImmortalityCurseEffect::new);

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<StructureType<?>> STRUCTURES = DeferredRegister.create(net.minecraft.core.Registry.STRUCTURE_TYPE_REGISTRY, MODID);

    // OVERWORLD OBELISK
    public static final RegistryObject<Block> RESPAWN_OBELISK_BLOCK = BLOCKS.register("respawn_obelisk", () -> new RespawnObeliskBlock(BlockBehaviour.Properties
            .of(Material.STONE)
            .noOcclusion()
            .strength(-1, 3600000.0F)
            .noLootTable(),
            Either.left(Level.OVERWORLD)
    ));
    public static final RegistryObject<Item> RESPAWN_OBELISK_ITEM = ITEMS.register("respawn_obelisk", () -> new BlockItem(RESPAWN_OBELISK_BLOCK.get(), new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(1)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));

    // NETHER OBELISK
    public static final RegistryObject<Block> RESPAWN_OBELISK_NETHER_BLOCK = BLOCKS.register("respawn_obelisk_nether", () -> new RespawnObeliskBlock(BlockBehaviour.Properties
            .of(Material.STONE)
            .noOcclusion()
            .strength(-1, 3600000.0F)
            .noLootTable(),
            Either.left(Level.NETHER)
    ));
    public static final RegistryObject<Item> RESPAWN_OBELISK_NETHER_ITEM = ITEMS.register("respawn_obelisk_nether", () -> new BlockItem(RESPAWN_OBELISK_NETHER_BLOCK.get(), new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(1)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));

    // END OBELISK
    public static final RegistryObject<Block> RESPAWN_OBELISK_END_BLOCK = BLOCKS.register("respawn_obelisk_end", () -> new RespawnObeliskBlock(BlockBehaviour.Properties
            .of(Material.STONE)
            .noOcclusion()
            .strength(-1, 3600000.0F)
            .noLootTable(),
            Either.left(Level.END)
    ));
    public static final RegistryObject<Item> RESPAWN_OBELISK_END_ITEM = ITEMS.register("respawn_obelisk_end", () -> new BlockItem(RESPAWN_OBELISK_END_BLOCK.get(), new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(1)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));

    // OBELISK CORE
    public static final RegistryObject<Item> OBELISK_CORE = ITEMS.register("obelisk_core", () -> new Item(new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));
    public static final RegistryObject<Item> OBELISK_CORE_NETHER = ITEMS.register("obelisk_core_nether", () -> new Item(new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));
    public static final RegistryObject<Item> OBELISK_CORE_END = ITEMS.register("obelisk_core_end", () -> new Item(new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));

    // DORMANT OBELISK
    public static final RegistryObject<Item> DORMANT_OBELISK = ITEMS.register("dormant_obelisk", () -> new Item(new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
    ));

    // FAKE RESPAWN ANCHOR
    public static final RegistryObject<Block> FAKE_ANCHOR_BLOCK = BLOCKS.register("fake_respawn_anchor", () -> new FakeRespawnAnchorBlock(BlockBehaviour.Properties
            .of(Material.STONE)
            .strength(-1, 3600000.0F)
            .noLootTable()
    ));

    // BLOCK ENTITIES
    public static final RegistryObject<BlockEntityType<RespawnObeliskBlockEntity>> RESPAWN_OBELISK_BE = BLOCK_ENTITIES.register("respawn_obelisk", () -> BlockEntityType.Builder.of(RespawnObeliskBlockEntity::new, RESPAWN_OBELISK_BLOCK.get()).build(null));

    //STRUCTURES
    public static final RegistryObject<StructureType<NetherLandStructures>> NETHER_STRUCTURES = STRUCTURES.register("nether_land_structure", () -> explicitStructureTypeTyping(NetherLandStructures.CODEC));

    private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(Codec<T> structureCodec) {
        return () -> structureCodec;
    }

    public static void register(IEventBus eventBus) {
        STRUCTURES.register(eventBus);
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MOB_EFFECTS.register(eventBus);
    }
}
