package com.redpxnda.respawnobelisks.registry;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> RESPAWN_OBELISKS = TagKey.of(RegistryKeys.BLOCK, new Identifier(MOD_ID, "respawn_obelisks"));
    }

    public static class Items {
        public static final TagKey<Item> OBELISK_CORES = TagKey.of(RegistryKeys.ITEM, new Identifier(MOD_ID, "obelisk_cores"));
        public static final TagKey<Item> RESPAWN_OBELISKS = TagKey.of(RegistryKeys.ITEM, new Identifier(MOD_ID, "respawn_obelisks"));
    }
}
