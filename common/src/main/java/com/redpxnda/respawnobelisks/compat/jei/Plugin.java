package com.redpxnda.respawnobelisks.compat.jei;

import com.redpxnda.respawnobelisks.data.recipe.CoreMergeRecipe;
import com.redpxnda.respawnobelisks.compat.jei.ObeliskInteractionCategory.*;
import com.redpxnda.respawnobelisks.data.recipe.CoreUpgradeRecipe;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import com.redpxnda.respawnobelisks.registry.ModTags;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.List;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

@JeiPlugin
public class Plugin implements IModPlugin {
    protected static RecipeType<InteractionRecipe> INTERACTION_RECIPE_TYPE = RecipeType.create(MOD_ID, "obelisk_interaction", InteractionRecipe.class);

    @Override
    public Identifier getPluginUid() {
        return new Identifier(MOD_ID, "jei_plugin");
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addCategoryExtension(CoreUpgradeRecipe.class, CoreUpgradeCategoryExtension::new);
        registration.getCraftingCategory().addCategoryExtension(CoreMergeRecipe.class, CoreMergeCategoryExtension::new);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        //registration.addRecipeCategories(new ObeliskInteractionCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<InteractionRecipe> recipes = new ArrayList<>();

//        ChargeConfig.getObeliskChargeItems().forEach((supp, charge) ->
//                recipes.add(new InteractionRecipe(supp.get().getDefaultInstance(), ModRegistries.RESPAWN_OBELISK_ITEM.get().getDefaultInstance(), charge)));
//
//        ChargeConfig.getNetherObeliskChargeItems().forEach((supp, charge) ->
//                recipes.add(new InteractionRecipe(supp.get().getDefaultInstance(), ModRegistries.RESPAWN_OBELISK_ITEM_NETHER.get().getDefaultInstance(), charge)));
//
//        ChargeConfig.getEndObeliskChargeItems().forEach((supp, charge) ->
//                recipes.add(new InteractionRecipe(supp.get().getDefaultInstance(), ModRegistries.RESPAWN_OBELISK_ITEM_END.get().getDefaultInstance(), charge)));

        registration.addRecipes(INTERACTION_RECIPE_TYPE, recipes);

        for (ItemStack stack : Ingredient.fromTag(ModTags.Items.OBELISK_CORES).getMatchingStacks()) {
            registration.addIngredientInfo(
                    stack,
                    VanillaTypes.ITEM_STACK,
                    Text.translatable("text.respawnobelisks.jei.core_info")
            );
        }

        for (ItemStack stack : Ingredient.fromTag(ModTags.Items.RESPAWN_OBELISKS).getMatchingStacks()) {
            registration.addIngredientInfo(
                    stack,
                    VanillaTypes.ITEM_STACK,
                    Text.translatable("text.respawnobelisks.jei.obelisk_info")
            );
        }

        registration.addIngredientInfo(
                ModRegistries.dormantObelisk.get().getDefaultStack(),
                VanillaTypes.ITEM_STACK,
                Text.translatable("text.respawnobelisks.jei.dormant_obelisk_info")
        );
    }
}
