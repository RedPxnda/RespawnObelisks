package com.redpxnda.respawnobelisks.mixin;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @WrapOperation(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V")
    )
    private void RESPAWNOBELISKS_translucentRender(EntityModel<LivingEntity> instance,
                                   MatrixStack poseStack,
                                   VertexConsumer vertexConsumer,
                                   int pPackedLight,
                                   int pPackedOverlay,
                                   float pRed,
                                   float pGreen,
                                   float pBlue,
                                   float pAlpha,
                                   Operation<Void> original,
                                   LivingEntity pEntity
    ) {
        if (pEntity instanceof PlayerEntity player && player.hasStatusEffect(ModRegistries.immortalityCurse.get())) {
            pAlpha *= 0.65f;
            pRed *= 0.75f;
            pGreen *= 0.75f;
            pBlue *= 0.75f;
        }
        original.call(instance, poseStack, vertexConsumer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
    }
}
