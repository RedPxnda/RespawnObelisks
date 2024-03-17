package com.redpxnda.respawnobelisks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.util.ClientUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @WrapOperation(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld$Properties;isHardcore()Z"))
    private boolean RESPAWNOBELISKS_allowHardcoreRespawn(ClientWorld.Properties instance, Operation<Boolean> original) {
        if (RespawnObelisksConfig.INSTANCE.allowHardcoreRespawning && ClientUtils.allowHardcoreRespawn) return false;
        return original.call(instance);
    }
}
