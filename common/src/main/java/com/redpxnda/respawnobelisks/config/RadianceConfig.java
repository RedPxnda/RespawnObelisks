package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.codec.tag.BlockList;
import com.redpxnda.nucleus.util.Comment;
import com.redpxnda.respawnobelisks.mixin.BeaconBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

@ConfigAutoCodec.ConfigClassMarker
public class RadianceConfig {
    @Comment("""
            The items used for charging an obelisk.
            Syntax: {"<ITEM_ID>": <CHARGE_AMOUNT>, ...}
            Ex:     {"minecraft:stick":35}""")
    public Map<Item, Double> chargingItems = new HashMap<>(Map.of(Items.ENDER_EYE, 25d, Items.ENDER_PEARL, 10d));

    @Comment("Whether dispensers can be used to charge obelisks.")
    public boolean allowDispenserCharging = false;

    @Comment("Sound to play when charging an obelisk.")
    public String chargingSound = "minecraft:block.respawn_anchor.charge";

    @Comment("Sound to play when removing radiance from an obelisk or respawning at an obelisk.")
    public String depletingSound = "minecraft:block.respawn_anchor.deplete";
    
    @Comment("Sound to play when setting your spawn at an obelisk.")
    public String spawnSettingSound = "minecraft:block.respawn_anchor.set_spawn";
    
    @Comment("How much radiance should be consumed when respawning at an obelisk. (a normal obelisk has 100 max radiance)")
    public double respawnCost = 20;
    
    @Comment("A list of blocks that can be used to provide an obelisk with infinite radiance.")
    public BlockList infiniteRadianceBlocks = BlockList.of(Blocks.BEACON);
    public boolean obeliskGetsInfiniteRadiance(Level world, BlockPos pos) {
        pos = pos.below();
        BlockState block = world.getBlockState(pos);
        boolean result = infiniteRadianceBlocks.contains(block);
        if (requiredBeaconLevel <= 0 || !result || !(block.getBlock() instanceof BeaconBlock)) return result;
        return world.getBlockEntity(pos) instanceof BeaconBlockEntityAccessor bbe && bbe.getBeaconPowerLevel() >= requiredBeaconLevel;
    }

    @Comment("The beacon level required for it to provide infinite radiance.")
    public int requiredBeaconLevel = 3;

    @Comment("Whether players can set their spawn at obelisks with no radiance.")
    public boolean allowEmptySpawnSetting = false;

    /*@Comment("""
            If the curse is enabled, this does nothing.
            This determines whether the player can respawn at an obelisk that has less charge than the cost to respawn.
            For example, if this is true, a player can still respawn at an obelisk with 1 charge, even though the cost of respawning is 20(by default)."""
    )*/ //todo implement? issue was with saving items.... (note potential >= bug)
    @AutoCodec.Ignored
    public boolean forgivingRespawn = true;
}
