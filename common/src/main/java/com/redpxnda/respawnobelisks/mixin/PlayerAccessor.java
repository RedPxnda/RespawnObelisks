package com.redpxnda.respawnobelisks.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerEntity.class)
public interface PlayerAccessor {
    @Invoker("vanishCursedItems")
    void destroyCursedItems();
}
