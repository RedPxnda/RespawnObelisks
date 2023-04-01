package com.redpxnda.respawnobelisks.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.redpxnda.respawnobelisks.scheduled.Task;
import com.redpxnda.respawnobelisks.scheduled.client.ClientRuneCircleTask;
import com.redpxnda.respawnobelisks.scheduled.client.ScheduledClientTasks;
import com.redpxnda.respawnobelisks.util.RenderUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(
            method = "renderLevel",
            at = @At("TAIL")
    )
    private void RESPAWNOBELISKS_renderRuneCircles(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) return;
        Player player = Minecraft.getInstance().player;
        for (Task task : ScheduledClientTasks.tasks) {
            if (task instanceof ClientRuneCircleTask runeTask) {
                if (runeTask.aabb.contains(player.getX(), player.getY(), player.getZ())) {
                    RenderUtils.renderRuneCircle(Minecraft.getInstance().level.getGameTime(), 1f, runeTask.pos, new PoseStack(), Minecraft.getInstance().renderBuffers().bufferSource(), LightTexture.FULL_BRIGHT);
                }
            }
        }
    }
}
