package com.redpxnda.respawnobelisks.config;

import com.mojang.datafixers.util.Either;
import com.teamresourceful.resourcefulconfig.common.annotations.Category;
import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

@Category(id = "charge", translation = "text.respawnobelisks.config.charge_config")
public final class ChargeConfig {
    private static Either<TagKey<Block>, Block> infiniteCharger = null;
    private static Map<Item, Double> chargeItems = null;
    @ConfigEntry(
            id = "obeliskChargeItems",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.charge_items"
    )
    @Comment("""
            The items used for charging an obelisk.
            Syntax: ["<ITEM_ID>|<CHARGE_AMOUNT>", ...]
            Ex:     ["minecraft:stick|35"]"""
    )
    public static String[] obeliskChargeItems = {"minecraft:ender_eye|25", "minecraft:ender_pearl|10"};
    public static Map<Item, Double> getChargeItems() {
        if (chargeItems != null) return chargeItems;
        chargeItems = new HashMap<>();
        for (String str : obeliskChargeItems) {
            String[] sections = str.split("\\|");
            chargeItems.put(BuiltInRegistries.ITEM.get(new ResourceLocation(sections[0])), Double.parseDouble(sections[1]));
        }
        return chargeItems;
    }

    @ConfigEntry(
            id = "obeliskChargeSound",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.obelisk_charge_sound"
    )
    @Comment("Sound to play when charging an obelisk.")
    public static String obeliskChargeSound = "minecraft:block.respawn_anchor.charge";

    @ConfigEntry(
            id = "obeliskDepleteSound",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.obelisk_deplete_sound"
    )
    @Comment("Sound to play when de-charging(negative charge value) or respawning at an obelisk.")
    public static String obeliskDepleteSound = "minecraft:block.respawn_anchor.deplete";

    @ConfigEntry(
            id = "obeliskSetSpawnSound",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.obelisk_set_spawn_sound"
    )
    @Comment("Sound to play when setting your spawn at an obelisk.")
    public static String obeliskSetSpawnSound = "minecraft:block.respawn_anchor.set_spawn";

    @ConfigEntry(
            id = "obeliskDepleteAmount",
            type = EntryType.INTEGER,
            translation = "text.respawnobelisks.config.obelisk_deplete_amount"
    )
    @Comment("How much charge (%) should be consumed when respawning at an obelisk.")
    public static int obeliskDepleteAmount = 20;

    @ConfigEntry(
            id = "infiniteChargeBlock",
            type = EntryType.STRING,
            translation = "text.respawnobelisks.config.infinite_charge_block"
    )
    @Comment("Block placed under an obelisk to allow for infinite charge. (Prefix with # for tags)")
    public static String infiniteChargeBlock = "minecraft:beacon";
    public static boolean isInfiniteCharger(BlockState block) {
        if (infiniteCharger == null) {
            if (infiniteChargeBlock.startsWith("#"))
                infiniteCharger = Either.left(TagKey.create(Registries.BLOCK, new ResourceLocation(infiniteChargeBlock.substring(1))));
            else
                infiniteCharger = Either.right(BuiltInRegistries.BLOCK.get(new ResourceLocation(infiniteChargeBlock)));
        }

        if (infiniteCharger.left().isPresent())
            return block.is(infiniteCharger.left().get());
        else
            return block.is(infiniteCharger.right().get());
    }

    @ConfigEntry(
            id = "allowEmptySpawnSetting",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.empty_spawn_setting"
    )
    @Comment("Whether players can set their spawn at obelisks without charge.")
    public static boolean allowEmptySpawnSetting = false;

    /*@ConfigEntry(
            id = "forgivingRespawn",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.forgiving_respawn" //todo implement? issue was with saving items....
    )
    @Comment("""
            If the curse is enabled, this does nothing.
            This determines whether the player can respawn at an obelisk that has less charge than the cost to respawn.
            For example, if this is true, a player can still respawn at an obelisk with 1 charge, even though the cost of respawning is 20(by default)."""
    )*/
    public static boolean forgivingRespawn = true;

/*    @ConfigEntry(
            id = "enablePerPlayerCharge",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.per_player_charge"
    )
    @Comment("""
            Whether obelisk charge is per-player.
            Keep in mind that charge still isn't global. Each obelisk just saves a charge for each player.
            Obelisks' max charges don't become per player. They stay per-obelisk.
            Note that this will break things like comparator support."""
    )
    public static boolean perPlayerCharge = false;*/
}
