package com.redpxnda.respawnobelisks.data.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.redpxnda.respawnobelisks.RespawnObelisks;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.item.CoreItem;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import org.slf4j.Logger;

import java.util.Optional;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

public class CoreUpgradeRecipe extends ShapedRecipe {
    private static final Logger LOGGER = RespawnObelisks.getLogger("Core Upgrading Recipe");

    private final ShapedRecipe compose;
    private final double charge;

    public CoreUpgradeRecipe(ShapedRecipe compose, double charge) {
        super(compose.getId(), compose.getGroup(), compose.getCategory(), compose.getWidth(), compose.getHeight(), compose.getIngredients(), compose.getOutput(null));
        this.compose = compose;
        this.charge = charge;
    }

    public ShapedRecipe getCompose() {
        return compose;
    }

    public double getCharge() {
        return charge;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager access) {
        ItemStack result = getCore(inv).orElse(super.craft(inv, access)).copy();
        result.setCount(1);
        CoreUtils.incMaxCharge(result.getOrCreateNbt(), charge);
        return result;
    }

    private static Optional<ItemStack> getCore(RecipeInputInventory inv) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.getItem() instanceof CoreItem)
                return Optional.of(stack);
        }

        return Optional.empty();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRegistries.coreUpgrade.get();
    }

    public static class Serializer extends ShapedRecipe.Serializer {
        @Override
        public CoreUpgradeRecipe read(Identifier resourceLocation, JsonObject jsonObject) {
            double charge = 100;
            if (jsonObject.has("charge"))
                if (jsonObject.get("charge") instanceof JsonPrimitive prim && prim.isNumber())
                    charge = prim.getAsDouble();
                else LOGGER.warn("(Respawn Obelisks) Recipe Json at '" + resourceLocation + "' has invalid 'charge' section.");
            return new CoreUpgradeRecipe(super.read(resourceLocation, jsonObject), charge);
        }

        @Override
        public CoreUpgradeRecipe read(Identifier resourceLocation, PacketByteBuf buf) {
            return new CoreUpgradeRecipe(super.read(resourceLocation, buf), buf.readDouble());
        }

        @Override
        public void write(PacketByteBuf buf, ShapedRecipe shapedRecipe) {
            super.write(buf, shapedRecipe);
            if (shapedRecipe instanceof CoreUpgradeRecipe recipe)
                buf.writeDouble(recipe.charge);
            else buf.writeDouble(100);
        }
    }
}
