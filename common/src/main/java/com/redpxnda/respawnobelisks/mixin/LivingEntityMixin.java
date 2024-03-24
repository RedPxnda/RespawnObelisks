package com.redpxnda.respawnobelisks.mixin;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.data.listener.ObeliskInteraction;
import com.redpxnda.respawnobelisks.facet.kept.KeptRespawnItems;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantValue")
@Mixin(value = LivingEntity.class, priority = 900)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "drop",
            at = @At("HEAD"),
            cancellable = true)
    private void RESPAWNOBELISKS_preventEquipmentDrop(DamageSource damageSource, CallbackInfo ci) {
        if (getCommandTags().contains("respawnobelisks:no_drops_entity"))
            ci.cancel();
        if (
                (Object) this instanceof ServerPlayerEntity player &&
                !player.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY) &&
                player.getSpawnPointPosition() != null &&
                player.getWorld().getBlockEntity(player.getSpawnPointPosition()) instanceof RespawnObeliskBlockEntity be &&
                CoreUtils.hasInteraction(be.getCoreInstance(), ObeliskInteraction.SAVE_INV) &&
                be.getCharge(player) >= RespawnObelisksConfig.INSTANCE.respawnPerks.minKeepItemRadiance &&
                (RespawnObelisksConfig.INSTANCE.respawnPerks.allowCursedItemKeeping || !player.hasStatusEffect(ModRegistries.immortalityCurse.get()))
        ) {
            KeptRespawnItems items = KeptRespawnItems.KEY.get(player);
            if (items != null) {
                boolean result = items.gather(player);
                if (!result) player.sendMessage(Text.translatable("text.respawnobelisks.cannot_save_items"));
            }
        }
    }
}
