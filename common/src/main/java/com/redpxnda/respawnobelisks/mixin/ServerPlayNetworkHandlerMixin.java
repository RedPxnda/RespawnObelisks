package com.redpxnda.respawnobelisks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.facet.HardcoreRespawningTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @WrapOperation(method = "onClientStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isHardcore()Z"))
    private boolean RESPAWNOBELISKS_allowHardcoreRespawn(MinecraftServer instance, Operation<Boolean> original) {
        if (RespawnObelisksConfig.INSTANCE.allowHardcoreRespawning) {
            HardcoreRespawningTracker tracker = HardcoreRespawningTracker.KEY.get(player);
            if (tracker != null && tracker.canRespawn) return false;
        }
        return original.call(instance);
    }
}
