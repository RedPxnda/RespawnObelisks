package com.redpxnda.respawnobelisks.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<List<String>> OBELISK_CHARGE_ITEM;
    public static final ForgeConfigSpec.ConfigValue<List<String>> OBELISK_CHARGE_ITEM_NETHER;
    public static final ForgeConfigSpec.ConfigValue<List<String>> OBELISK_CHARGE_ITEM_END;
    public static final ForgeConfigSpec.ConfigValue<String> OBELISK_CHARGE_SOUND;
    public static final ForgeConfigSpec.ConfigValue<Integer> OBELISK_DEPLETE_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<String> OBELISK_DEPLETE_SOUND;
    public static final ForgeConfigSpec.ConfigValue<String> OBELISK_SET_RESPAWN_SOUND;
    public static final ForgeConfigSpec.ConfigValue<String> OBELISK_REMOVAL_SOUND;
    public static final ForgeConfigSpec.ConfigValue<String> INFINITE_CHARGE_BLOCK;
    public static final ForgeConfigSpec.ConfigValue<String> REMOVAL_ITEM;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ALLOW_PICKUP;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_CURSE;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_CURSE_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<Integer> CURSE_LEVEL_INCREMENT;
    public static final ForgeConfigSpec.ConfigValue<Integer> CURSE_DURATION;
    public static final ForgeConfigSpec.ConfigValue<String> CURSE_SOUND;

    static {
        BUILDER.push("Server Config for Respawn Obelisks");

        List<String> itemArray = new ArrayList<>();
        itemArray.add("minecraft:ender_pearl|10");
        itemArray.add("minecraft:ender_eye|25");
        OBELISK_CHARGE_ITEM = BUILDER.comment("The items used for charging the obelisk.\n" +
                        "Syntax: [\"<ITEM_ID>|<CHARGE_AMOUNT>\", ...]\n" +
                        "        [\"<ITEM_ID>|<CHARGE_AMOUNT>|<ALLOW_OVERFILL>\", ...]\n" +
                        "Ex:     [\"minecraft:stick|-1|false\"]\n" +
                        "The 'ALLOW_OVERFILL' option is a boolean that determines whether the player\n" +
                        "is allowed to waste their item in order to get a portion of the actual charge amount.\n" +
                        "For example, if the charge amount is 3 and your obelisk is already charged twice,\n" +
                        "the charge amount WOULD go to 5. However, since the max is 3, it only goes to 3.\n" +
                        "The same logic is applied to going below 0 with negative charge values.")
                        .define("Obelisk Charge Items", itemArray);
        OBELISK_CHARGE_ITEM_NETHER = BUILDER.define("Nether Obelisk Charge Items", itemArray);
        OBELISK_CHARGE_ITEM_END = BUILDER.define("End Obelisk Charge Items", itemArray);
        OBELISK_CHARGE_SOUND = BUILDER.comment("The sound played when charging an obelisk.").define("Obelisk Charge Sound", "minecraft:block.respawn_anchor.charge");
        OBELISK_DEPLETE_AMOUNT = BUILDER.comment("The amount of charge to consume when respawning at an Obelisk.").define("Obelisk Deplete Amount", 20);
        OBELISK_DEPLETE_SOUND = BUILDER.comment("The sound played when decharging an obelisk. (Eg. An item reduces charge amount)").define("Obelisk Deplete Sound", "minecraft:block.respawn_anchor.deplete");
        OBELISK_SET_RESPAWN_SOUND = BUILDER.comment("The sound played when setting your spawn at an obelisk.").define("Obelisk Set Spawn Sound", "minecraft:block.respawn_anchor.set_spawn");
        OBELISK_REMOVAL_SOUND = BUILDER.comment("Sound played when destroying an obelisk. See 'Obelisk Removal Item'.").define("Obelisk Removal Sound", "minecraft:block.beacon.deactivate");
        INFINITE_CHARGE_BLOCK = BUILDER.comment("The block used to give obelisks an infinite amount of charge.").define("Infinite Charge Block", "minecraft:beacon");
        REMOVAL_ITEM = BUILDER.comment("The item that destroys the obelisk when used on it.\nIf 'Allow Obelisk Pickup' is enabled, this will drop a Dormant Obelisk.")
                .define("Obelisk Removal Item", "minecraft:totem_of_undying");
        ALLOW_PICKUP = BUILDER.comment("Whether obelisks can be picked up or not. See 'Obelisk Removal Item'.")
                .define("Allow Obelisk Pickup", true);
        ENABLE_CURSE = BUILDER.comment("Whether the Curse of Immortality effect is enabled.\n" +
                        "The player will receive this effect when spawning at an obelisk with no charge.")
                .define("Enable Immortality Curse", true);
        MAX_CURSE_LEVEL = BUILDER.comment("The maximum level the curse effect can stack up to.\n" +
                        "Each level is 1 removed heart, and therefore at level 9 you are left at 1 heart.")
                .define("Immortality Curse Max Level", 5);
        CURSE_LEVEL_INCREMENT = BUILDER.comment("The amount each death will increment the curse by.\n" +
                        "Level 2 means every death will remove 2 hearts.")
                .define("Immortality Curse Death Increment", 2);
        CURSE_DURATION = BUILDER.comment("The duration of the Curse of Immortality effect after dying. (In ticks)")
                .define("Immortality Curse Duration", 6000);
        CURSE_SOUND = BUILDER.comment("Sound played when respawning with the Immortality Curse.")
                .define("Curse Respawn Sound", "minecraft:entity.elder_guardian.curse");

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
