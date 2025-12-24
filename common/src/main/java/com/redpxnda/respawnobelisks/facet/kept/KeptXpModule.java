package com.redpxnda.respawnobelisks.facet.kept;

import com.redpxnda.nucleus.util.PlayerUtil;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;

public class KeptXpModule implements KeptItemsModule {
    public int xp = 0;

    @Override
    public Tag toNbt() {
        return IntTag.valueOf(xp);
    }

    @Override
    public void fromNbt(Tag element) {
        if (!(element instanceof IntTag nbtInt)) return;
        xp = nbtInt.getAsInt();
    }

    @Override
    public void restore(ServerPlayer oldPlayer, ServerPlayer player) {
        if (xp == 0) return;
        player.giveExperiencePoints(xp);
        xp = 0;
    }

    @Override
    public void gather(ServerPlayer player) {
        if (xp <= 0 && !player.wasExperienceConsumed() && RespawnObelisksConfig.INSTANCE.respawnPerks.experience.keepExperience) {
            int rawXp = PlayerUtil.getTotalXp(player);
            xp = Mth.floor(rawXp*(RespawnObelisksConfig.INSTANCE.respawnPerks.experience.keepExperiencePercent/100f));
            if (RespawnObelisksConfig.INSTANCE.respawnPerks.experience.keepExperiencePercent >= 100) player.skipDropExperience();
            else player.giveExperiencePoints(-xp);
        }
    }

    @Override
    public void scatter(double x, double y, double z, ServerPlayer player) {
        if (xp <= 0) return;
        ExperienceOrb orb = new ExperienceOrb(player.level(), x, y, z, xp);
        player.level().addFreshEntity(orb);
        xp = 0;
    }

    @Override
    public boolean isEmpty() {
        return xp <= 0;
    }
}
