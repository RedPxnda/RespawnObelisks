package com.redpxnda.respawnobelisks.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> RESPAWN_OBELISKS = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "respawn_obelisks"));
    }

    public static class Items {
        public static final TagKey<Item> OBELISK_CORES = TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "obelisk_cores"));
        public static final TagKey<Item> RESPAWN_OBELISKS = TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "respawn_obelisks"));
    }
}
