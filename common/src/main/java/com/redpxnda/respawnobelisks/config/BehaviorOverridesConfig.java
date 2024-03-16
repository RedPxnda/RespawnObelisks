package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.codec.tag.BlockList;
import com.redpxnda.nucleus.util.Comment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;

import java.util.List;

@ConfigAutoCodec.ConfigClassMarker
public class BehaviorOverridesConfig {
    @Comment("""
            A list of blocks that players cannot respawn at.
            Tags are supported. To use them, use a hashtag before the name. Eg. #minecraft:beds
            Note: This will not prevent spawn point from being set at the block with commands. If anything "forces" the respawn point, it won't be prevented."""
    )
    public BlockList bannedRespawnBlocks = new BlockList(List.of(Blocks.RESPAWN_ANCHOR), List.of(BlockTags.BEDS));

    public boolean isBlockBanned(BlockState block) {
        return bannedRespawnBlocks.contains(block);
    }

    @Comment("Whether vanilla's nether respawn anchors should be repurposed and turned into 'Destruction Catalysts'")
    public boolean destructionCatalysts = true;
}
