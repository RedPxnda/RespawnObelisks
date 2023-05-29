package com.redpxnda.respawnobelisks.compat.jei;

import com.redpxnda.respawnobelisks.config.ChargeConfig;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

@JeiPlugin
public class Plugin implements IModPlugin {
    protected static RecipeType<ObeliskInteractionCategory.InteractionRecipe> INTERACTION_RECIPE_TYPE = RecipeType.create(MOD_ID, "obelisk_interaction", ObeliskInteractionCategory.InteractionRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MOD_ID, "jei_plugin");
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addCategoryExtension(CoreUpgradeRecipe.class, CoreUpgradeCategoryExtension::new);
        registration.getCraftingCategory().addCategoryExtension(CoreMergeRecipe.class, CoreMergeCategoryExtension::new);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ObeliskInteractionCategory(registration.getJeiHelpers().getGuiHelper()));
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

        for (ItemStack stack : Ingredient.of(ModTags.Items.OBELISK_CORES).getItems()) {
            registration.addIngredientInfo(
                    stack,
                    VanillaTypes.ITEM_STACK,
                    Component.translatable("text.respawnobelisks.jei.core_info")
            );
        }

        for (ItemStack stack : Ingredient.of(ModTags.Items.RESPAWN_OBELISKS).getItems()) {
            registration.addIngredientInfo(
                    stack,
                    VanillaTypes.ITEM_STACK,
                    Component.translatable("text.respawnobelisks.jei.obelisk_info")
            );
        }

        registration.addIngredientInfo(
                ModRegistries.DORMANT_OBELISK.get().getDefaultInstance(),
                VanillaTypes.ITEM_STACK,
                Component.translatable("text.respawnobelisks.jei.dormant_obelisk_info")
        );
    }
}
