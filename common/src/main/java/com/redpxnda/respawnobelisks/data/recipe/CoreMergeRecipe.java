package com.redpxnda.respawnobelisks.data.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.redpxnda.respawnobelisks.RespawnObelisks;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.item.CoreItem;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

public class CoreMergeRecipe extends ShapelessRecipe {
    private static final Logger LOGGER = RespawnObelisks.getLogger("Core Merging Recipe");

    private final ShapelessRecipe compose;
    private final double multiplier;

    public CoreMergeRecipe(ShapelessRecipe compose, double multiplier) {
        super(compose.getId(), compose.getGroup(), compose.getCategory(), compose.getOutput(null), compose.getIngredients());
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
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager access) {
        List<ItemStack> stacks = getCores(inv);
        if (stacks.isEmpty()) return super.craft(inv, access);

        double totalCharge = CoreUtils.getMaxCharge(stacks.get(0).getOrCreateNbt());
        for (ItemStack stack : stacks.subList(1, stacks.size()))
            totalCharge += CoreUtils.getMaxCharge(stack.getOrCreateNbt())*multiplier;

        ItemStack result = stacks.get(0).copy();
        result.setCount(1);
        CoreUtils.setMaxCharge(result.getOrCreateNbt(), totalCharge);
        return result;
    }

    private static List<ItemStack> getCores(RecipeInputInventory inv) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
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
        public CoreMergeRecipe read(Identifier resourceLocation, JsonObject jsonObject) {
            double multiplier = 1;
            if (jsonObject.has("multiplier"))
                if (jsonObject.get("multiplier") instanceof JsonPrimitive prim && prim.isNumber())
                    multiplier = prim.getAsDouble();
                else LOGGER.warn("Recipe Json at '" + resourceLocation + "' has invalid 'multiplier' section.");
            return new CoreMergeRecipe(super.read(resourceLocation, jsonObject), multiplier);
        }

        @Override
        public CoreMergeRecipe read(Identifier resourceLocation, PacketByteBuf buf) {
            return new CoreMergeRecipe(super.read(resourceLocation, buf), buf.readDouble());
        }

        @Override
        public void write(PacketByteBuf buf, ShapelessRecipe shapelessRecipe) {
            super.write(buf, shapelessRecipe);
            if (shapelessRecipe instanceof CoreMergeRecipe recipe)
                buf.writeDouble(recipe.multiplier);
            else buf.writeDouble(1);
        }
    }
}
