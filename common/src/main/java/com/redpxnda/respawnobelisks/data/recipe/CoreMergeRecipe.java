package com.redpxnda.respawnobelisks.data.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.logging.LogUtils;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.item.CoreItem;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CoreMergeRecipe extends ShapelessRecipe {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ShapelessRecipe compose;
    private final double multiplier;

    public CoreMergeRecipe(ShapelessRecipe compose, double multiplier) {
        super(compose.getId(), compose.getGroup(), compose.category(), compose.getResultItem(null), compose.getIngredients());
        this.compose = compose;
        this.multiplier = multiplier;
    }

    public ShapelessRecipe getCompose() {
        return compose;
    }

    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
        List<ItemStack> stacks = getCores(inv);
        if (stacks.isEmpty()) return super.assemble(inv, access);

        double totalCharge = CoreUtils.getMaxCharge(stacks.get(0).getOrCreateTag());
        for (ItemStack stack : stacks.subList(1, stacks.size()))
            totalCharge += CoreUtils.getMaxCharge(stack.getOrCreateTag())*multiplier;

        ItemStack result = stacks.get(0).copy();
        result.setCount(1);
        CoreUtils.setMaxCharge(result.getOrCreateTag(), totalCharge);
        return result;
    }

    private static List<ItemStack> getCores(CraftingContainer inv) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof CoreItem)
                stacks.add(stack);
        }

        return stacks;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRegistries.coreMerge.get();
    }

    public static class Serializer extends ShapelessRecipe.Serializer {
        @Override
        public CoreMergeRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            double multiplier = 1;
            if (jsonObject.has("multiplier"))
                if (jsonObject.get("multiplier") instanceof JsonPrimitive prim && prim.isNumber())
                    multiplier = prim.getAsDouble();
                else LOGGER.warn("(Respawn Obelisks) Recipe Json at '" + resourceLocation + "' has invalid 'multiplier' section.");
            return new CoreMergeRecipe(super.fromJson(resourceLocation, jsonObject), multiplier);
        }

        @Override
        public CoreMergeRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf buf) {
            return new CoreMergeRecipe(super.fromNetwork(resourceLocation, buf), buf.readDouble());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ShapelessRecipe shapelessRecipe) {
            super.toNetwork(buf, shapelessRecipe);
            if (shapelessRecipe instanceof CoreMergeRecipe recipe)
                buf.writeDouble(recipe.multiplier);
            else buf.writeDouble(1);
        }
    }
}
