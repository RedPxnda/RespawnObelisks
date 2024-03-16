package com.redpxnda.respawnobelisks.mixin;

import net.minecraft.entity.ExperienceOrbEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExperienceOrbEntity.class)
public interface ExperienceOrbAccessor {
    @Accessor
    int getOrbAge();

    @Accessor
    void setOrbAge(int age);
}
