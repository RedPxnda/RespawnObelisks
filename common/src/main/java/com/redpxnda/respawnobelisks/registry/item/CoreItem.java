package com.redpxnda.respawnobelisks.registry.item;

import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class CoreItem extends Item {
    public CoreItem(Settings properties) {
        super(properties);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> lines, TooltipContext tooltipFlag) {
        if (stack.hasNbt() && stack.getNbt().contains("RespawnObeliskData")) {
            int pos = 1;
            if (stack.getNbt().getCompound("RespawnObeliskData").contains("Charge"))
                lines.add(pos++,
                        Text.translatable("text.respawnobelisks.tooltip.charge").formatted(Formatting.GRAY)
                                .append(Text.literal(" " + CoreUtils.getCharge(stack.getNbt())).formatted(Formatting.WHITE))
                );
            if (stack.getNbt().getCompound("RespawnObeliskData").contains("MaxCharge")) {
                lines.add(pos++,
                        Text.translatable("text.respawnobelisks.tooltip.max_charge").formatted(Formatting.GRAY)
                                .append(Text.literal(" " + CoreUtils.getTextMaxCharge(stack.getNbt())).formatted(Formatting.WHITE))
                );
            }
            if (stack.getNbt().getCompound("RespawnObeliskData").contains("SavedEntities")) {
                lines.add(pos++,
                        Text.translatable("text.respawnobelisks.tooltip.saved_entities").formatted(Formatting.GRAY)
                                .append(Text.literal(" [").formatted(Formatting.DARK_GRAY))
                                .append(Text.literal("CTRL").formatted(Screen.hasControlDown() ? Formatting.WHITE : Formatting.GRAY))
                                .append(Text.literal("]").formatted(Formatting.DARK_GRAY))
                );
                if (Screen.hasControlDown()) {
                    NbtList list = stack.getNbt().getCompound("RespawnObeliskData").getList("SavedEntities", 10);
                    for (NbtElement tag : list) {
                        if (!(tag instanceof NbtCompound compound) || compound.isEmpty() || !compound.contains("type")) continue;
                        String type = Registries.ENTITY_TYPE.get(Identifier.tryParse(compound.getString("type"))).getTranslationKey();
                        MutableText component = compound.contains("data") && compound.getCompound("data").contains("CustomName") ?
                                Text.Serializer.fromJson(compound.getCompound("data").getString("CustomName")) :
                                null;
                        MutableText finalComponent = Text.literal(" | ").formatted(Formatting.GRAY)
                                .append(Text.translatable(type).formatted(Formatting.WHITE));
                        if (component != null)
                            finalComponent = finalComponent
                                    .append(Text.literal(" (Name: ").formatted(Formatting.WHITE))
                                    .append(component.formatted(Formatting.GRAY))
                                    .append(Text.literal(")").formatted(Formatting.WHITE));
                        lines.add(pos++, finalComponent);
                    }
                }
            }
            if (stack.getNbt().getCompound("RespawnObeliskData").contains("TrustedPlayers")) {
                lines.add(pos++,
                        Text.translatable("text.respawnobelisks.tooltip.trusted_players").formatted(Formatting.GRAY)
                                .append(Text.literal(" [").formatted(Formatting.DARK_GRAY))
                                .append(Text.literal("ALT").formatted(Screen.hasAltDown() ? Formatting.WHITE : Formatting.GRAY))
                                .append(Text.literal("]").formatted(Formatting.DARK_GRAY))
                );
                if (Screen.hasAltDown()) {
                    NbtList list = stack.getNbt().getCompound("RespawnObeliskData").getList("TrustedPlayers", 8);
                    for (NbtElement tag : list) {
                        if (!(tag instanceof NbtString stringTag)) continue;
                        lines.add(pos++,
                                Text.literal(" | ").formatted(Formatting.GRAY)
                                        .append(Text.literal(stringTag.toString()).formatted(Formatting.WHITE))
                        );
                    }
                }
            }
        }
    }

    public static ItemStack createTabItem(Item item) {
        ItemStack stack = item.getDefaultStack();
        CoreUtils.setMaxCharge(stack.getOrCreateNbt(), 100);
        CoreUtils.setCharge(stack.getOrCreateNbt(), 100);
        return stack;
    }
}