package com.redpxnda.respawnobelisks.facet;

import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class FailedSpawnBlocks implements EntityFacet<ListTag> {
    public static FacetKey<FailedSpawnBlocks> KEY;

    public final Set<Block> blocks = new HashSet<>();

    @Override
    public ListTag toNbt() {
        ListTag list = new ListTag();
        for (Block block : blocks)
            list.add(StringTag.valueOf(BuiltInRegistries.BLOCK.getKey(block).toString()));
        return list;
    }

    @Override
    public void loadNbt(ListTag nbt) {
        for (Tag element : nbt) {
            if (element instanceof StringTag nbtStr) {
                String strId = nbtStr.getAsString();
                ResourceLocation id = ResourceLocation.tryParse(strId);
                if (id != null) {
                    Block block = BuiltInRegistries.BLOCK.get(id);
                    blocks.add(block);
                }
            }
        }
    }
}
