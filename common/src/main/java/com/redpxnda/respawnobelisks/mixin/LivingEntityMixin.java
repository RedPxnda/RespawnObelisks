package com.redpxnda.respawnobelisks.mixin;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.data.listener.ObeliskInteraction;
import com.redpxnda.respawnobelisks.facet.kept.KeptRespawnItems;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantValue")
@Mixin(value = LivingEntity.class, priority = 900)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(
            method = "dropAllDeathLoot",
            at = @At("HEAD"),
            cancellable = true)
    private void RESPAWNOBELISKS_preventEquipmentDrop(DamageSource damageSource, CallbackInfo ci) {
        if (getTags().contains("respawnobelisks:no_drops_entity"))
            ci.cancel();
        if (
                (Object) this instanceof ServerPlayer player &&
                !player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) &&
                player.getRespawnPosition() != null &&
                player.level().getBlockEntity(player.getRespawnPosition()) instanceof RespawnObeliskBlockEntity be &&
                CoreUtils.hasInteraction(be.getCoreInstance(), ObeliskInteraction.SAVE_INV) &&
                be.getCharge(player) >= RespawnObelisksConfig.INSTANCE.respawnPerks.minKeepItemRadiance &&
                (RespawnObelisksConfig.INSTANCE.respawnPerks.allowCursedItemKeeping || !player.hasEffect(ModRegistries.immortalityCurse.get()))
        ) {
            KeptRespawnItems items = KeptRespawnItems.KEY.get(player);
            if (items != null) {
                boolean result = items.gather(player);
                if (!result) player.sendSystemMessage(Component.translatable("text.respawnobelisks.cannot_save_items"));
            }
        }
    }
}
