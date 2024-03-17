package com.redpxnda.respawnobelisks.mixin;

import com.redpxnda.nucleus.util.Color;
import com.redpxnda.respawnobelisks.facet.SecondarySpawnPoints;
import com.redpxnda.respawnobelisks.network.FinishPriorityChangePacket;
import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.RespawnAtWorldSpawnPacket;
import com.redpxnda.respawnobelisks.util.ClientUtils;
import com.redpxnda.respawnobelisks.util.SpawnPoint;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
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
        super(Text.empty());
    }

    @Shadow @Final private List<ButtonWidget> buttons;

    @Shadow private int ticksSinceDeath;

    @Shadow protected abstract void setButtonsActive(boolean active);

    @Inject(method = "init", at = @At("TAIL"))
    private void RESPAWNOBELISKS_alternativeSpawnButton(CallbackInfo ci) {
        SecondarySpawnPoints facet = SecondarySpawnPoints.KEY.get(client.player);
        if (facet != null) {
            List<SpawnPoint> choices = new ArrayList<>();
            if (facet.canChooseRespawn) {
                if (facet.canChooseWorldSpawn) choices.add(null);
                choices.addAll(facet.points);
            } else if (facet.canChooseWorldSpawn) {
                choices.add(null);
                choices.add(facet.getLatestPoint());
            } else return;

            ButtonWidget button = addDrawableChild(new ButtonWidget(width / 2 - 124, height / 4 + 72, 20, 20, Text.empty(), wid -> {
                ticksSinceDeath = 0;
                setButtonsActive(false);
                if (choices.isEmpty()) return;
                Collections.rotate(choices, 1);
                SpawnPoint point = choices.get(choices.size()-1);
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
                protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
                    if (choices.isEmpty()) return;
                    SpawnPoint point = choices.get(choices.size()-1);

                    Item item;
                    Text text;
                    Text dimensionText;

                    if (point == null) {
                        text = Text.translatable("text.respawnobelisks.world_spawn").setStyle(Style.EMPTY.withColor(Formatting.WHITE));
                        dimensionText = Text.literal(World.OVERWORLD.getValue().toString()).setStyle(Style.EMPTY.withColor(Formatting.GRAY));
                        item = Items.GRASS_BLOCK;
                    } else {
                        item = ClientUtils.cachedSpawnPointItems.getOrDefault(point, Items.AIR);
                        text = Text.translatable(item.getTranslationKey()).append(Text.literal(" @(" + point.pos().getX() + ", " + point.pos().getY() + ", " + point.pos().getZ() + ")")).setStyle(Style.EMPTY.withColor(Formatting.WHITE));
                        dimensionText = Text.literal(point.dimension().getValue().toString()).setStyle(Style.EMPTY.withColor(Formatting.GRAY));
                    }
                    context.drawItem(item.getDefaultStack(), getX(), getY());
                    if (isHovered()) {
                        context.fill(getX()-2, getY()-2, getX()+18, getY()-1, Color.WHITE.argb());
                        context.fill(getX()+17, getY()-2, getX()+18, getY()+18, Color.WHITE.argb());
                        context.fill(getX()-2, getY()+17, getX()+18, getY()+18, Color.WHITE.argb());
                        context.fill(getX()-2, getY()-2, getX()-1, getY()+18, Color.WHITE.argb());
                        context.drawTooltip(client.textRenderer, List.of(text, dimensionText), getX(), getY());
                    }
                }
            });
            buttons.add(button);
            button.active = false;
        }
    }
}
