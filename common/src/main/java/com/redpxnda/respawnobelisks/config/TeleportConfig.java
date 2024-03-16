package com.redpxnda.respawnobelisks.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.util.Comment;

@ConfigAutoCodec.ConfigClassMarker
public class TeleportConfig {
    @Comment("Whether players can teleport to obelisks by binding a recovery compass to a lodestone under the obelisk.")
    public boolean enableTeleportation = true;

    @Comment("The delay before being able to teleport again. (In ticks)\nKeep this above 100, otherwise issues will arise.\nDefault value: 3 minutes/3600 ticks")
    public int teleportationCooldown = 3600;

    @Comment("The delay before being able to teleport again after an unsuccessful teleport. (Eg. player moves or switches items, etc.")
    public int teleportationBackupCooldown = 200;

    @Comment("Whether players drop their items when teleporting. If enabled, all enabled perks in the 'perks' section will apply.")
    public boolean dropItemsOnTeleport = false;

    @Comment("Whether players drop their recovery compass when teleporting.")
    public boolean dropCompassOnTp = true;

    @Comment("The amount of experience(points, not levels) consumed when teleporting to an obelisk.\nUseful numbers: https://minecraft.fandom.com/wiki/Experience#Leveling_up (See 'Total XP')\nDefault value: 27 points/3 levels")
    public int xpCost = 27;

    @Comment("The amount of experience(levels, not points) consumed when teleporting to an obelisk.\nThis is different to 'xpCost' as the cost to teleport will technically get more expensive the more levels you have.\nThis is similar to how Waystones works.")
    public int levelCost = 0;

    @Comment("Whether players can teleport whilst having the immortality curse.")
    public boolean allowCursedTeleportation = false;

    @Comment("Whether the immortality curse should be forcefully applied when teleporting.\nRegardless of this value, you will still receive the curse when teleporting to uncharged obelisks.")
    public boolean forcedCurseOnTp = false;

    @Comment("Minimum radiance required to teleport. Values of 0 or lower will disable this requirement.")
    public double minimumTpRadiance = 1;

    @Comment("Amount of radiance lost when teleporting to an obelisk.")
    public double teleportationCost = 20;
}
