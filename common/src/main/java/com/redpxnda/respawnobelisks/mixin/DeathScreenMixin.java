package com.redpxnda.respawnobelisks.mixin;

import com.redpxnda.nucleus.util.Color;
import com.redpxnda.respawnobelisks.facet.SecondarySpawnPoints;
import com.redpxnda.respawnobelisks.network.FinishPriorityChangePacket;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.RespawnAtWorldSpawnPacket;
import com.redpxnda.respawnobelisks.util.ClientUtils;
import com.redpxnda.respawnobelisks.util.SpawnPoint;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin extends Screen {
    protected DeathScreenMixin() {
        super(Component.empty());
    }

    @Shadow @Final private List<Button> exitButtons;

    @Shadow private int delayTicker;

    @Shadow protected abstract void setButtonsActive(boolean active);

    @Inject(method = "init", at = @At("TAIL"))
    private void RESPAWNOBELISKS_alternativeSpawnButton(CallbackInfo ci) {
        SecondarySpawnPoints facet = SecondarySpawnPoints.KEY.get(minecraft.player);
        if (facet != null) {
            List<SpawnPoint> choices = new ArrayList<>();
            if (facet.canChooseRespawn) {
                if (facet.canChooseWorldSpawn) choices.add(null);
                for (SpawnPoint point : facet.points) {
                    if (ClientUtils.cachedSpawnPointBlocks.containsKey(point))
                        choices.add(point);
                }
            } else if (facet.canChooseWorldSpawn) {
                choices.add(null);
                choices.add(facet.getLatestPoint());
            } else return;

            addRenderableWidget(new Button(width / 2 - 124, height / 4 + 72, 20, 20, Component.empty(), wid -> {
                delayTicker = 0;
                setButtonsActive(false);
                if (choices.isEmpty()) return;
                Collections.rotate(choices, 1);
                SpawnPoint point = choices.get(choices.size() - 1);
                if (point == null)
                    ModPackets.CHANNEL.sendToServer(new RespawnAtWorldSpawnPacket(true));
                else {
                    ModPackets.CHANNEL.sendToServer(new RespawnAtWorldSpawnPacket(false));
                    List<SpawnPoint> finalList = new ArrayList<>(choices);
                    finalList.remove(null);
                    ModPackets.CHANNEL.sendToServer(new FinishPriorityChangePacket(finalList));
                }
            }, Supplier::get) {
                @Override
                protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
                    if (choices.isEmpty()) return;
                    SpawnPoint point = choices.get(choices.size() - 1);

                    ItemStack item;
                    Component text;
                    Component positionText;

                    if (point == null) {
                        text = Component.translatable("text.respawnobelisks.world_spawn").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
                        positionText = Component.literal(Level.OVERWORLD.location().toString()).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
                        item = Items.GRASS_BLOCK.getDefaultInstance();
                        item.setHoverName(Component.literal("World Spawn").setStyle(Style.EMPTY.withItalic(true)));
                    } else {
                        item = ClientUtils.cachedSpawnPointItems.getOrDefault(point, ItemStack.EMPTY);
                        text = item.getHoverName();
                        positionText = Component.literal(point.dimension().location() + " @(" + point.pos().getX() + ", " + point.pos().getY() + ", " + point.pos().getZ() + ")").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
                    }
                    context.renderItem(item, getX(), getY());
                    if (isHovered()) {
                        context.fill(getX() - 2, getY() - 2, getX() + 18, getY() - 1, Color.WHITE.argb());
                        context.fill(getX() + 17, getY() - 2, getX() + 18, getY() + 18, Color.WHITE.argb());
                        context.fill(getX() - 2, getY() + 17, getX() + 18, getY() + 18, Color.WHITE.argb());
                        context.fill(getX() - 2, getY() - 2, getX() - 1, getY() + 18, Color.WHITE.argb());
                        context.renderComponentTooltip(minecraft.font, List.of(text, positionText), getX(), getY());
                    }
                }
            });
        }
    }
}
