package com.redpxnda.respawnobelisks.compat.jei;

import com.redpxnda.respawnobelisks.data.recipe.CoreUpgradeRecipe;
import com.redpxnda.respawnobelisks.registry.item.CoreItem;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.logging.log4j.core.Core;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class CoreUpgradeCategoryExtension implements ICraftingCategoryExtension {
    private final CoreUpgradeRecipe recipe;

    public CoreUpgradeCategoryExtension(CoreUpgradeRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        NonNullList<Ingredient> inputs = recipe.getIngredients();
        int target = -1;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, x * 18 + 1, y * 18 + 1);

                if (x + y*3 < inputs.size()) {
                    ItemStack[] items = inputs.get(x + y * 3).getItems();
                    boolean fire = false;
                    for (int i = 0; i < items.length; i++) {
                        ItemStack stack = items[i].copy();
                        if (target <= -1 && stack.getItem() instanceof CoreItem) {
                            CoreUtils.setMaxCharge(stack.getOrCreateTag(), "x");
                            fire = true;
                        }
                        slotBuilder.addItemStack(stack);
                    }
                    if (fire) target = x + y * 3;
                }
            }
        }

        IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19);
        if (target <= -1) return;
        ItemStack[] items = inputs.get(target).getItems();
        for (int i = 0; i < items.length; i++) {
            ItemStack stack = items[i].copy();
            CoreUtils.setMaxCharge(stack.getOrCreateTag(), "x + " + recipe.getCharge());
            slotBuilder.addItemStack(stack);
        }
    }

    @Override
    public @Nullable ResourceLocation getRegistryName() {
        return recipe.getId();
    }
}
