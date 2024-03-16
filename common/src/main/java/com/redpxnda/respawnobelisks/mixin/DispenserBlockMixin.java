package com.redpxnda.respawnobelisks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DispenserBlock.class)
public class DispenserBlockMixin {
    @WrapOperation(method = "dispense", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/DispenserBlock;getBehaviorForItem(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/block/dispenser/DispenserBehavior;"))
    private DispenserBehavior RESPAWNOBELISKS_redirectDispenserOutput(DispenserBlock instance, ItemStack stack, Operation<DispenserBehavior> original, ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (
                RespawnObelisksConfig.INSTANCE.radiance.allowDispenserCharging &&
                RespawnObelisksConfig.INSTANCE.radiance.chargingItems.containsKey(stack.getItem()) &&
                state.getBlock() instanceof DispenserBlock &&
                world.getBlockEntity(pos.offset(state.get(DispenserBlock.FACING))) instanceof RespawnObeliskBlockEntity robe
        )
            return RespawnObeliskBlock.DISPENSER_BEHAVIOR.apply(robe);
        return original.call(instance, stack);
    }
}
