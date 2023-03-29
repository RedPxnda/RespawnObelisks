package com.redpxnda.respawnobelisks.event;

import com.redpxnda.respawnobelisks.network.ModPackets;
import com.redpxnda.respawnobelisks.network.ScrollWheelPacket;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.block.RespawnObeliskBlock;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBER;
import com.redpxnda.respawnobelisks.registry.block.entity.RespawnObeliskBlockEntity;
import com.redpxnda.respawnobelisks.registry.particle.ParticlePack;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTextureStitchEvent;
import dev.architectury.event.events.client.ClientTooltipEvent;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ClientEvents {
    protected static void onItemTooltip(ItemStack stack, List<Component> lines, TooltipFlag flag) {
        if (stack.hasTag() && stack.getTag().contains("RespawnObeliskData")) {
            if (stack.getTag().getCompound("RespawnObeliskData").contains("Charge"))
                lines.add(
                        Component.translatable("text.respawnobelisks.tooltip.charge").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" " + RespawnObeliskBlockEntity.getCharge(stack.getTag())).withStyle(ChatFormatting.WHITE))
                );
            if (stack.getTag().getCompound("RespawnObeliskData").contains("MaxCharge"))
                lines.add(
                        Component.translatable("text.respawnobelisks.tooltip.max_charge").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" " + RespawnObeliskBlockEntity.getMaxCharge(stack.getTag())).withStyle(ChatFormatting.WHITE))
                );
            if (stack.getTag().getCompound("RespawnObeliskData").contains("SavedEntities")) {
                lines.add(
                        Component.translatable("text.respawnobelisks.tooltip.saved_entities").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(" [").withStyle(ChatFormatting.DARK_GRAY))
                                .append(Component.literal("CTRL").withStyle(Screen.hasControlDown() ? ChatFormatting.WHITE : ChatFormatting.GRAY))
                                .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY))
                );
                if (Screen.hasControlDown()) {
                    ListTag list = stack.getTag().getCompound("RespawnObeliskData").getList("SavedEntities", 10);
                    for (Tag tag : list) {
                        if (!(tag instanceof CompoundTag compound) || compound.isEmpty() || !compound.contains("type")) continue;
                        String type = Registry.ENTITY_TYPE.get(ResourceLocation.tryParse(compound.getString("type"))).getDescriptionId();
                        MutableComponent component = compound.contains("data") && compound.getCompound("data").contains("CustomName") ?
                                Component.Serializer.fromJson(compound.getCompound("data").getString("CustomName")) :
                                null;
                        MutableComponent finalComponent = Component.literal(" | ").withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable(type).withStyle(ChatFormatting.WHITE));
                        if (component != null)
                            finalComponent = finalComponent
                                    .append(Component.literal(" (Name: ").withStyle(ChatFormatting.WHITE))
                                    .append(component.withStyle(ChatFormatting.GRAY))
                                    .append(Component.literal(")").withStyle(ChatFormatting.WHITE));
                        lines.add(finalComponent);
                    }
                }
            }
            if (stack.getTag().getCompound("RespawnObeliskData").contains("TrustedPlayers")) {
                lines.add(
                        Component.translatable("text.respawnobelisks.tooltip.trusted_players").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(" [").withStyle(ChatFormatting.DARK_GRAY))
                                .append(Component.literal("ALT").withStyle(Screen.hasAltDown() ? ChatFormatting.WHITE : ChatFormatting.GRAY))
                                .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY))
                );
                if (Screen.hasAltDown()) {
                    ListTag list = stack.getTag().getCompound("RespawnObeliskData").getList("TrustedPlayers", 8);
                    for (Tag tag : list) {
                        if (!(tag instanceof StringTag stringTag)) continue;
                        lines.add(
                                Component.literal(" | ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(stringTag.toString()).withStyle(ChatFormatting.WHITE))
                        );
                    }
                }
            }
        }
    }

    protected static EventResult onClientScroll(Minecraft mc, double amount) {
        LocalPlayer player = mc.player;
        if (player == null || !player.isShiftKeyDown()) return EventResult.pass();
        if (mc.hitResult instanceof BlockHitResult blockResult && mc.level != null) {
            BlockState blockState = mc.level.getBlockState(blockResult.getBlockPos());
            if (!(blockState.getBlock() instanceof RespawnObeliskBlock)) return EventResult.pass();
            boolean isUpper = false;
            if (!(blockState.getValue(RespawnObeliskBlock.HALF) == DoubleBlockHalf.LOWER)) isUpper = true;
            mc.level.playSound(player, blockResult.getBlockPos(), SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, 1, 1);
            ModPackets.CHANNEL.sendToServer(new ScrollWheelPacket(amount, blockResult, isUpper));
            return EventResult.interruptFalse();
        }

        return EventResult.pass();
    }

    protected static void onTextureStitch(TextureAtlas atlas, Consumer<ResourceLocation> spriteAdder) {
        if (!atlas.location().equals(TextureAtlas.LOCATION_BLOCKS)) return;
        spriteAdder.accept(RespawnObeliskBER.RUNES);
        ParticlePack.getPackTextures().forEach((str, loc) -> spriteAdder.accept(loc));
    }

    public static void onClientSetup(Minecraft mc) {
        BlockEntityRendererRegistry.register(ModRegistries.RESPAWN_OBELISK_BE.get(), RespawnObeliskBER::new);
    }

    public static void init() {
        ClientRawInputEvent.MOUSE_SCROLLED.register(ClientEvents::onClientScroll);
        ClientTextureStitchEvent.PRE.register(ClientEvents::onTextureStitch);
        ClientLifecycleEvent.CLIENT_SETUP.register(ClientEvents::onClientSetup);
        ClientTooltipEvent.ITEM.register(ClientEvents::onItemTooltip);
    }
}
