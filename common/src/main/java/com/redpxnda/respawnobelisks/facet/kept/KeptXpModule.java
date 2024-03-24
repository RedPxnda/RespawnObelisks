package com.redpxnda.respawnobelisks.facet.kept;

import com.redpxnda.nucleus.util.PlayerUtil;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class KeptXpModule implements KeptItemsModule {
    public int xp = 0;

    @Override
    public NbtElement toNbt() {
        return NbtInt.of(xp);
    }

    @Override
    public void fromNbt(NbtElement element) {
        if (!(element instanceof NbtInt nbtInt)) return;
        xp = nbtInt.intValue();
    }

    @Override
    public void restore(ServerPlayerEntity player) {
        if (xp == 0) return;
        player.addExperience(xp);
        xp = 0;
    }

    @Override
    public void gather(ServerPlayerEntity player) {
        if (xp <= 0 && !player.isExperienceDroppingDisabled() && RespawnObelisksConfig.INSTANCE.respawnPerks.experience.keepExperience) {
            int rawXp = PlayerUtil.getTotalXp(player);
            xp = MathHelper.floor(rawXp*(RespawnObelisksConfig.INSTANCE.respawnPerks.experience.keepExperiencePercent/100f));
            if (RespawnObelisksConfig.INSTANCE.respawnPerks.experience.keepExperiencePercent >= 100) player.disableExperienceDropping();
            else player.addExperience(-xp);
        }
    }

    @Override
    public void scatter(double x, double y, double z, ServerPlayerEntity player) {
        if (xp <= 0) return;
        ExperienceOrbEntity orb = new ExperienceOrbEntity(player.getWorld(), x, y, z, xp);
        player.getWorld().spawnEntity(orb);
        xp = 0;
    }

    @Override
    public boolean isEmpty() {
        return xp <= 0;
    }
}
