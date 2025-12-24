package com.redpxnda.respawnobelisks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @WrapOperation(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;isHardcore()Z"))
    private boolean RESPAWNOBELISKS_allowHardcoreRespawn(ClientLevel.ClientLevelData instance, Operation<Boolean> original) {
        if (RespawnObelisksConfig.INSTANCE.allowHardcoreRespawning && ClientUtils.allowHardcoreRespawn) return false;
        return original.call(instance);
    }
}
