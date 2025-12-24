package com.redpxnda.respawnobelisks.registry.item;

import com.redpxnda.respawnobelisks.config.RespawnObelisksConfig;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RadiantLanternItem extends BlockItem {
    public RadiantLanternItem(Block block, Properties settings) {
        super(block, settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag tooltipFlag) {
        double charge = CoreUtils.getCharge(stack.getOrCreateTag());
        lines.add(1,
                Component.translatable("text.respawnobelisks.tooltip.charge").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" " + charge).withStyle(ChatFormatting.WHITE))
        );
        lines.add(1, Component.translatable("text.respawnobelisks.tooltip.radiant_lantern." + (charge > 0 ? "full" : "empty")).withStyle(ChatFormatting.GRAY));
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        return CoreUtils.getCharge(context.getItemInHand().getOrCreateTag()) > 0 && super.canPlace(context, state);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        int prevCount = stack.getCount();

        InteractionResult result = super.place(context);
        int postCount = stack.getCount();

        if (context.getPlayer() != null && RespawnObelisksConfig.INSTANCE.radiantFlame.allowMultipleUses && prevCount > postCount) {
            stack.setCount(1);
            ItemStack newStack = stack.copy(); // prevent item from being lost
            stack.setCount(postCount);
            CoreUtils.setCharge(newStack.getOrCreateTag(), 0);
            context.getPlayer().getInventory().placeItemBackInInventory(newStack);
        }

        return result;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level world, @Nullable Player player, ItemStack stack, BlockState state) {
        if (player != null) player.getCooldowns().addCooldown(stack.getItem(), RespawnObelisksConfig.INSTANCE.radiantFlame.placementCooldown);
        return super.updateCustomBlockEntityTag(pos, world, player, stack, state);
    }
}
