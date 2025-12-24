package com.redpxnda.respawnobelisks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.util.ClientUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
    @WrapOperation(method = "handlePlayerCombatKill", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;isHardcore()Z"))
    private boolean RESPAWNOBELISKS_allowHardcoreRespawn(ClientLevel.ClientLevelData instance, Operation<Boolean> original) {
        if (RespawnObelisksConfig.INSTANCE.allowHardcoreRespawning && ClientUtils.allowHardcoreRespawn) return false;
        return original.call(instance);
    }
}
