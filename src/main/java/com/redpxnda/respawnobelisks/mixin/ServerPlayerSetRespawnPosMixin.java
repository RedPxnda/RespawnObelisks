package com.redpxnda.respawnobelisks.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true, print = true)
@Mixin(ServerPlayer.class)
public class ServerPlayerSetRespawnPosMixin {

    @WrapWithCondition(
            method = "startSleepInBed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;FZZ)V")
    )
    private boolean setRespawnPositionCondition(ServerPlayer instance) {
        System.out.println("My code runs, and yours does not!");
        return false;
    }
}
