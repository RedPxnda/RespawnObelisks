package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.tag.BlockList;
import com.redpxnda.nucleus.codec.tag.TaggableBlock;
import com.redpxnda.nucleus.config.preset.ConfigProvider;
import com.redpxnda.nucleus.util.Comment;
import com.redpxnda.nucleus.util.MiscUtil;
import com.redpxnda.respawnobelisks.registry.ModTags;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;

import java.util.function.Supplier;

public enum ConfigPresets implements ConfigProvider<RespawnObelisksConfig> {
    @Comment("A more forgiving preset with less expense and less difficulty.")
    FORGIVING(() -> MiscUtil.initialize(new RespawnObelisksConfig(), config -> {
        config.immortalityCurse.curseDuration /= 2;
        config.immortalityCurse.curseLevelIncrement /= 2;

        config.radiance.allowDispenserCharging = true;
        config.radiance.respawnCost /= 2;
        config.radiance.requiredBeaconLevel = 0;

        config.teleportation.teleportationCost /= 2;
        config.teleportation.teleportationBackupCooldown = 60;
        config.teleportation.allowCursedTeleportation = true;

        config.revival.revivalCost /= 2;

        config.cores.maxMaxRadiance += 250;

        config.respawnPerks.enchantment.tradeable = true;
        config.respawnPerks.enchantment.chancePerLevel = 33.4;
        config.respawnPerks.experience.keepExperience = true;
        config.respawnPerks.experience.keepExperiencePercent = 10;
    })),

    @Comment("""
             A preset to make the mod tack on to vanilla's respawning rather than completely overriding it.
             Allows players to have multiple respawn points and makes obelisks give passive respawn perks.""")
    VANILLA_COMPLEMENTARY(() -> MiscUtil.initialize(new RespawnObelisksConfig(), config -> {
        config.behaviorOverrides.bannedRespawnBlocks = BlockList.of();
        config.behaviorOverrides.destructionCatalysts = false;

        config.secondarySpawnPoints.enableSecondarySpawnPoints = true;
        config.secondarySpawnPoints.worldSpawnMode = SecondarySpawnPointConfig.PointSpawnMode.IF_UNCHARGED_OBELISK;

        config.respawnPerks.experience.keepExperience = true;
        config.respawnPerks.inventory.keepInventory = true;
        config.respawnPerks.inventory.keepInventoryChance = 5;
        config.respawnPerks.hotbar.keepHotbar = true;
        config.respawnPerks.hotbar.keepHotbarChance = 5;
        config.respawnPerks.armor.keepArmor = true;
        config.respawnPerks.armor.keepArmorChance = 10;
        config.respawnPerks.enchantment.maxLevel = 1;
        config.respawnPerks.enchantment.chancePerLevel = 100;
    })),

    @Comment("""
             A preset to make obelisks act as "checkpoints."
             Players can have multiple respawn points, but obelisks take priority.""")
    OBELISKS_AS_CHECKPOINTS(() -> MiscUtil.initialize(new RespawnObelisksConfig(), config -> {
        config.behaviorOverrides.bannedRespawnBlocks = BlockList.of();
        config.behaviorOverrides.destructionCatalysts = false;

        config.secondarySpawnPoints.enableSecondarySpawnPoints = true;

        config.secondarySpawnPoints.maxPointsPerBlock.put(new TaggableBlock(BlockTags.BEDS), 1);
        config.secondarySpawnPoints.maxPointsPerBlock.put(new TaggableBlock(Blocks.RESPAWN_ANCHOR), 1);
        config.secondarySpawnPoints.maxPointsPerBlock.put(new TaggableBlock(ModTags.Blocks.RESPAWN_OBELISKS), 1);
        config.secondarySpawnPoints.defaultMaxPoints = 1;

        config.secondarySpawnPoints.allowPriorityShifting = false;
        config.secondarySpawnPoints.enableBlockPriorities = true;
        config.secondarySpawnPoints.blockPriorities.put(new TaggableBlock(ModTags.Blocks.RESPAWN_OBELISKS), 10f);

        config.secondarySpawnPoints.worldSpawnMode = SecondarySpawnPointConfig.PointSpawnMode.IF_UNCHARGED_OBELISK;
        config.secondarySpawnPoints.secondarySpawnMode = SecondarySpawnPointConfig.PointSpawnMode.IF_UNCHARGED_OBELISK;
    }));

    private final Supplier<RespawnObelisksConfig> supplier;

    ConfigPresets(Supplier<RespawnObelisksConfig> supplier) {
        this.supplier = supplier;
    }

    @Override
    public RespawnObelisksConfig getInstance() {
        return supplier.get();
    }
}
