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
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.slf4j.Logger;

import java.util.Optional;

public class CoreUpgradeRecipe extends ShapedRecipe {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ShapedRecipe compose;
    private final double charge;

    public CoreUpgradeRecipe(ShapedRecipe compose, double charge) {
        super(compose.getId(), compose.getGroup(), compose.category(), compose.getWidth(), compose.getHeight(), compose.getIngredients(), compose.getResultItem(null));
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
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
        ItemStack result = getCore(inv).orElse(super.assemble(inv, access)).copy();
        result.setCount(1);
        CoreUtils.incMaxCharge(result.getOrCreateTag(), charge);
        return result;
    }

    private static Optional<ItemStack> getCore(CraftingContainer inv) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
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
        public CoreUpgradeRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            double charge = 100;
            if (jsonObject.has("charge"))
                if (jsonObject.get("charge") instanceof JsonPrimitive prim && prim.isNumber())
                    charge = prim.getAsDouble();
                else LOGGER.warn("(Respawn Obelisks) Recipe Json at '" + resourceLocation + "' has invalid 'charge' section.");
            return new CoreUpgradeRecipe(super.fromJson(resourceLocation, jsonObject), charge);
        }

        @Override
        public CoreUpgradeRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf buf) {
            return new CoreUpgradeRecipe(super.fromNetwork(resourceLocation, buf), buf.readDouble());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ShapedRecipe shapedRecipe) {
            super.toNetwork(buf, shapedRecipe);
            if (shapedRecipe instanceof CoreUpgradeRecipe recipe)
                buf.writeDouble(recipe.charge);
            else buf.writeDouble(100);
        }
    }
}
