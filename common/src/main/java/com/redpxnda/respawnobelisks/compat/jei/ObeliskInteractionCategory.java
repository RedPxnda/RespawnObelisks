package com.redpxnda.respawnobelisks.compat.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import java.util.List;

import static com.redpxnda.respawnobelisks.RespawnObelisks.MOD_ID;

public class ObeliskInteractionCategory implements IRecipeCategory<ObeliskInteractionCategory.InteractionRecipe> {
    public static final Identifier JEI_BACKGROUND = new Identifier(MOD_ID, "textures/gui/jei_category.png");

    private final IDrawable background;
    private final IDrawable icon;

    public ObeliskInteractionCategory(IGuiHelper helper) {
        this.background = helper
                .drawableBuilder(JEI_BACKGROUND, 0, 0, 116, 32)
                .setTextureSize(128, 128)
                .build();
        this.icon = helper.createDrawableItemStack(ModRegistries.respawnObeliskItem.get().getDefaultStack());
//        this.slotBackground = helper
//                .drawableBuilder(new ResourceLocation("jei", "textures/gui/slot.png"), 0, 0, 18, 18)
//                .setTextureSize(18, 18)
//                .build();
    }

    @Override
    public RecipeType<InteractionRecipe> getRecipeType() {
        return Plugin.INTERACTION_RECIPE_TYPE;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("text.respawnobelisks.jei.interaction_title");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, InteractionRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 7, 8);
        inputSlot.addItemStack(recipe.input);

        IRecipeSlotBuilder obeliskSlot = builder.addSlot(RecipeIngredientRole.CATALYST, 53, 8);
        obeliskSlot.addItemStack(recipe.obelisk);
    }

    //todo jei stuff with the core new system
    @Override
    public void draw(InteractionRecipe recipe, IRecipeSlotsView slotsView, DrawContext graphics, double mouseX, double mouseY) {
        /*double charge = Mth.clamp(recipe.charge, 0, 100)/100f;

        RenderSystem.setShaderTexture(0, JEI_BACKGROUND);
        graphics.blit(stack, 98, 4, 0, 0, 32, 16, 22, 128, 128);
        graphics.blit(stack, 103, 7+Mth.ceil(14*(1-(charge))), 0, 5, 57+Mth.ceil(14*(1-(charge))), 6, Mth.ceil(14*(charge)), 128, 128);*/
    }

    @Override
    public List<Text> getTooltipStrings(InteractionRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX >= 98 && mouseX <= 113 && mouseY >= 4 && mouseY <= 25)
            return List.of(
                    Text.literal(recipe.charge + " ").formatted(Formatting.GOLD)
                    .append(Text.translatable("text.respawnobelisks.jei.charge_text").formatted(Formatting.DARK_GRAY))
            );

        return IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
    }

    public static class InteractionRecipe {
        private final ItemStack input;
        private final ItemStack obelisk;
        private final double charge;

        public InteractionRecipe(ItemStack input, ItemStack obelisk, double charge) {
            this.input = input;
            this.obelisk = obelisk;
            this.charge = charge;
        }
    }
}
