package com.redpxnda.respawnobelisks.config;

import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.Config;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.annotations.InlineCategory;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;
import com.teamresourceful.resourcefulconfig.web.annotations.WebInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

@Config("respawnobelisks")
@WebInfo(icon = "smartphone-charging")
public final class RespawnObelisksConfig {
    private static List<Block> bannedBlocks = new ArrayList<>();

    @ConfigEntry(
            id = "allowBedRespawning",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.allow_bed_respawning"
    )
    @Comment("""
            Whether players should be able to set their spawn at beds.
            This has no effect on whether players can sleep or not. Only disallows setting spawn at beds.
            MAKE SURE TO REMOVE '#minecraft:beds' FROM 'bannedRespawnBlocks'!"""
    )
    public static boolean allowBedRespawning = false;

    @ConfigEntry(
            id = "bannedRespawnBlocks",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.allow_bed_respawning"
    )
    @Comment("""
            A list of blocks that players cannot respawn from. This is mainly to enforce 'allowBedRespawning'.
            Keep in mind that adding modded blocks to this list will not prevent the player from 'setting' their spawn at that block.
            It will only prevent them from spawning their when respawning.
            Tags are supported. To use them, use a hashtag before the name. Eg. #minecraft:beds"""
    )
    public static String[] bannedRespawnBlocks = {"#minecraft:beds"};

    public static boolean isBlockBanned(BlockState block) {
        if (bannedBlocks.isEmpty()) {
            for (String str : bannedRespawnBlocks) {
                if (str.startsWith("#")) {
                    str = str.substring(1);
                    ResourceLocation loc = ResourceLocation.tryParse(str);
                    if (loc == null) continue;
                    TagKey<Block> tag = TagKey.create(Registries.BLOCK, loc);
                    if (BuiltInRegistries.BLOCK.getTag(tag).isPresent()) {
                        for (Holder<Block> blockHolder : BuiltInRegistries.BLOCK.getTag(tag).get()) {
                            bannedBlocks.add(blockHolder.value());
                        }
                    }
                } else {
                    ResourceLocation loc = ResourceLocation.tryParse(str);
                    if (loc == null) continue;
                    BuiltInRegistries.BLOCK.getOptional(loc).ifPresent(b -> bannedBlocks.add(b));
                }
            }
        }
        return bannedBlocks.contains(block.getBlock());
    }

    @InlineCategory
    public static CurseConfig curseConfig;

    @InlineCategory
    public static ChargeConfig chargeConfig;

    @InlineCategory
    public static TeleportConfig teleportConfig;

    @InlineCategory
    public static ReviveConfig reviveConfig;

    @InlineCategory
    public static TrustedPlayersConfig trustedPlayersConfig;

    @InlineCategory
    public static ObeliskCoreConfig obeliskCoreConfig;

    @InlineCategory
    public static RespawnPerkConfig respawnPerkConfig;

    @InlineCategory
    public static ObeliskDimensionsConfig obeliskDimensionsConfig;
}
