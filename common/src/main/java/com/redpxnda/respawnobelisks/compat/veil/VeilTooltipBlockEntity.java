package com.redpxnda.respawnobelisks.compat.veil;

public class VeilTooltipBlockEntity /*extends RespawnObeliskBlockEntity implements Tooltippable*/ {
    /*public List<Component> tooltip = Arrays.asList(
            Component.translatable("block.respawnobelisks.respawn_obelisk").withStyle(ChatFormatting.GOLD),
            Component.literal(Strings.repeat('|', 50)).withStyle(ChatFormatting.GREEN)
    );
    private static ColorTheme theme = new ColorTheme();
    static {
        theme.addColor("background", Color.VANILLA_TOOLTIP_BACKGROUND);
        theme.addColor("topBorder", Color.VANILLA_TOOLTIP_BORDER_TOP);
        theme.addColor("bottomBorder", Color.VANILLA_TOOLTIP_BORDER_BOTTOM);
    }

    public TooltipBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
        this.loadConsumers.add(tag -> this.updateChargeTooltip());
    }

    @Override
    public List<Component> getTooltip() {
        return this.tooltip;
    }

    @Override
    public void setTooltip(List<Component> tooltip) {
        if (tooltip != null) this.tooltip = tooltip;
    }

    @Override
    public void addTooltip(Component tooltip) {
        if (tooltip != null) this.tooltip.add(tooltip);
    }

    @Override
    public void addTooltip(List<Component> tooltip) {
        if (tooltip != null) this.tooltip.addAll(tooltip);
    }

    @Override
    public void addTooltip(String tooltip) {
        if (tooltip != null) this.tooltip.add(Component.literal(tooltip));
    }

    @Override
    public ColorTheme getTheme() {
        return theme;
    }

    @Override
    public void setTheme(ColorTheme newTheme) {
        if (newTheme != null) theme = newTheme;
    }

    @Override
    public void setBackgroundColor(int color) {
        theme.getColors().add(0, Color.of(color));
    }

    @Override
    public void setTopBorderColor(int color) {
        theme.getColors().add(1, Color.of(color));
    }

    @Override
    public void setBottomBorderColor(int color) {
        theme.getColors().add(2, Color.of(color));
    }

    @Override
    public boolean getWorldspace() {
        return true;
    }

    @Override
    public TooltipTimeline getTimeline() {
        return new TooltipTimeline(new TooltipKeyframe[] {}, 1.0f);
    }

    @Override
    public ItemStack getStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public int getTooltipWidth() {
        return 0;
    }

    @Override
    public int getTooltipHeight() {
        return 8;
    }

    @Override
    public int getTooltipXOffset() {
        return 0;
    }

    @Override
    public int getTooltipYOffset() {
        return 0;
    }

    @Override
    public List<VeilUIItemTooltipDataHolder> getItems() {
        return List.of(new VeilUIItemTooltipDataHolder(ItemStack.EMPTY, (f) -> 0f, (f) -> 0f));
    }

    public void updateChargeTooltip() {
        int maxIcons = 50;
        int chargedIcons = Math.round((float)((this.getCharge(null)/this.getMaxCharge())*maxIcons));
        String str = Strings.repeat('|', chargedIcons);
        String str2 = Strings.repeat('|', maxIcons-chargedIcons);
        Component component = Component.literal(str).withStyle(ChatFormatting.GRAY).append(Component.literal(str2).withStyle(ChatFormatting.DARK_GRAY));
        this.tooltip.set(1, component);
    }*/
}
