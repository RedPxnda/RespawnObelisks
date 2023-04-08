package com.redpxnda.respawnobelisks.mixin;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V")
    )
    private void RESPAWNOBELISKS_translucentRender(EntityModel<LivingEntity> instance,
                                   PoseStack poseStack,
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
        if (pEntity instanceof Player player && player.hasEffect(ModRegistries.IMMORTALITY_CURSE.get())) {
            pAlpha *= 0.65;
            pRed *= 0.75;
            pGreen *= 0.75;
            pBlue *= 0.75;
        }
        instance.renderToBuffer(poseStack, vertexConsumer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
    }
}
