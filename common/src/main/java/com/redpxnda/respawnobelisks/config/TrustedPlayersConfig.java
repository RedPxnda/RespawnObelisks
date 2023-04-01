package com.redpxnda.respawnobelisks.config;

import com.teamresourceful.resourcefulconfig.common.annotations.Category;
import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;

@Category(id = "trustedPlayers", translation = "text.respawnobelisks.config.trusted_players_config")
public final class TrustedPlayersConfig {
    @ConfigEntry(
            id = "enablePlayerTrust",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.enable_player_trust"
    )
    @Comment("Whether players can be trusted/banned from accessing certain obelisks.")
    public static boolean enablePlayerTrust = true;

    @ConfigEntry(
            id = "allowObeliskBreaking",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.allow_obelisk_breaking"
    )
    @Comment("Whether untrusted players can break the obelisk.")
    public static boolean allowObeliskBreaking = false;

    @ConfigEntry(
            id = "allowObeliskRespawning",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.allow_obelisk_respawning"
    )
    @Comment("Whether untrusted players can respawn at the obelisk.")
    public static boolean allowObeliskRespawning = false;

    @ConfigEntry(
            id = "allowObeliskInteraction",
            type = EntryType.BOOLEAN,
            translation = "text.respawnobelisks.config.allow_obelisk_interaction"
    )
    @Comment("Whether untrusted players can interact with the obelisk.")
    public static boolean allowObeliskInteraction = false;
}
