package com.redpxnda.respawnobelisks.registry.item;

import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CoreItem extends Item {
    public CoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag tooltipFlag) {
        if (stack.hasTag() && stack.getTag().contains("RespawnObeliskData")) {
            int pos = 1;
            if (stack.getTag().getCompound("RespawnObeliskData").contains("Charge"))
                lines.add(pos++,
                        Component.translatable("text.respawnobelisks.tooltip.charge").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(" " + CoreUtils.getCharge(stack.getTag())).withStyle(ChatFormatting.WHITE))
                );
            if (stack.getTag().getCompound("RespawnObeliskData").contains("MaxCharge")) {
                lines.add(pos++,
                        Component.translatable("text.respawnobelisks.tooltip.max_charge").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(" " + CoreUtils.getTextMaxCharge(stack.getTag())).withStyle(ChatFormatting.WHITE))
                );
            }
            if (stack.getTag().getCompound("RespawnObeliskData").contains("SavedEntities")) {
                lines.add(pos++,
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
                        lines.add(pos++, finalComponent);
                    }
                }
            }
            if (stack.getTag().getCompound("RespawnObeliskData").contains("TrustedPlayers")) {
                lines.add(pos++,
                        Component.translatable("text.respawnobelisks.tooltip.trusted_players").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(" [").withStyle(ChatFormatting.DARK_GRAY))
                                .append(Component.literal("ALT").withStyle(Screen.hasAltDown() ? ChatFormatting.WHITE : ChatFormatting.GRAY))
                                .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY))
                );
                if (Screen.hasAltDown()) {
                    ListTag list = stack.getTag().getCompound("RespawnObeliskData").getList("TrustedPlayers", 8);
                    for (Tag tag : list) {
                        if (!(tag instanceof StringTag stringTag)) continue;
                        lines.add(pos++,
                                Component.literal(" | ").withStyle(ChatFormatting.GRAY)
                                        .append(Component.literal(stringTag.toString()).withStyle(ChatFormatting.WHITE))
                        );
                    }
                }
            }
        }
    }

    @Override
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> list) {
        if (this.allowedIn(creativeModeTab)) {
            ItemStack stack = this.getDefaultInstance();
            CoreUtils.setMaxCharge(stack.getOrCreateTag(), 100);
            CoreUtils.setCharge(stack.getOrCreateTag(), 100);
            list.add(stack);
        }
    }
}