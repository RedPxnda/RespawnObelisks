package com.redpxnda.respawnobelisks.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> RESPAWN_OBELISKS = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MOD_ID, "respawn_obelisks"));
    }

    public static class Items {
        public static final TagKey<Item> OBELISK_CORES = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MOD_ID, "obelisk_cores"));
        public static final TagKey<Item> OVERWORLD_CORES = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MOD_ID, "overworld_cores"));
        public static final TagKey<Item> NETHER_CORES = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MOD_ID, "nether_cores"));
        public static final TagKey<Item> END_CORES = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MOD_ID, "end_cores"));
        public static final TagKey<Item> RESPAWN_OBELISKS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MOD_ID, "respawn_obelisks"));
    }
}
