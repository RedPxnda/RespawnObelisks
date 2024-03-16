package com.redpxnda.respawnobelisks.compat.jei;

import com.redpxnda.respawnobelisks.data.recipe.CoreMergeRecipe;
import com.redpxnda.respawnobelisks.registry.item.CoreItem;
import com.redpxnda.respawnobelisks.util.CoreUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoreMergeCategoryExtension implements ICraftingCategoryExtension {
    private final CoreMergeRecipe recipe;

    public CoreMergeCategoryExtension(CoreMergeRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        DefaultedList<Ingredient> ingredients = recipe.getIngredients();
        List<String> characters = new ArrayList<>(List.of(new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i"}));
        List<String> usedCharacters = new ArrayList<>();
        List<List<ItemStack>> inputs = new ArrayList<>(ingredients.stream().map(i -> Arrays.asList(i.getMatchingStacks())).toList());
        List<ItemStack> outputs = new ArrayList<>();
        int target = -1;
        for (List<ItemStack> list : inputs) {
            boolean ran = false;
            for (ItemStack stack : list) {
                if (!(stack.getItem() instanceof CoreItem)) continue;
                CoreUtils.setMaxCharge(stack.getOrCreateNbt(), characters.get(0));
                ran = true;
                if (target == -1) {
                    ItemStack copy = stack.copy();
                    outputs.add(copy);
                }
            }
            if (ran) {
                if (target == -1) target = inputs.indexOf(list);
                usedCharacters.add(characters.get(0));
                characters.remove(0);
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, x * 18 + 1, y * 18 + 1);

                if (x + y*3 < inputs.size()) {
                    slotBuilder.addItemStacks(inputs.get(x + y*3));
                }
            }
        }

        StringBuilder output = new StringBuilder("a");
        for (String str : usedCharacters.subList(1, usedCharacters.size()))
            output.append(String.format("+%.2f%s", recipe.getMultiplier(), str));

        IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19);
        if (target != -1) {
            outputs.forEach(stack -> CoreUtils.setMaxCharge(stack.getOrCreateNbt(), output.toString()));
            slotBuilder.addItemStacks(outputs);
        }

        builder.setShapeless();
    }

    @Override
    public @Nullable Identifier getRegistryName() {
        return recipe.getId();
    }
}
