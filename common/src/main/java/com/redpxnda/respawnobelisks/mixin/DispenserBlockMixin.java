package com.redpxnda.respawnobelisks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DispenserBlock.class)
public class DispenserBlockMixin {
    @WrapOperation(method = "dispenseFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/DispenserBlock;getDispenseMethod(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/core/dispenser/DispenseItemBehavior;"))
    private DispenseItemBehavior RESPAWNOBELISKS_redirectDispenserOutput(DispenserBlock instance, ItemStack stack, Operation<DispenseItemBehavior> original, ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (
                RespawnObelisksConfig.INSTANCE.radiance.allowDispenserCharging &&
                RespawnObelisksConfig.INSTANCE.radiance.chargingItems.containsKey(stack.getItem()) &&
                state.getBlock() instanceof DispenserBlock &&
                world.getBlockEntity(pos.relative(state.getValue(DispenserBlock.FACING))) instanceof RespawnObeliskBlockEntity robe
        )
            return RespawnObeliskBlock.DISPENSER_BEHAVIOR.apply(robe);
        return original.call(instance, stack);
    }
}
