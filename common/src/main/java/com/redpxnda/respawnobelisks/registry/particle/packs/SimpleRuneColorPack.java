package com.redpxnda.respawnobelisks.registry.particle.packs;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleRuneColorPack implements IBasicPack {
    public List<float[]> colors = new ArrayList<>();
    public int ticks = 100;

    @Override
    public float[] runeColor(float partialTick, Level level) {
        if (colors.isEmpty()) return new float[] { 1, 1, 1 };
        int time = (int) (level.getGameTime() % ticks*2);
        if (time >= ticks) time = time-ticks;

        int colorIndex = (int) Math.floor((time/(float)ticks)*colors.size());
        float progress = ((time/(float)ticks)*colors.size())-colorIndex;

        boolean tooLarge = colorIndex+1 >= colors.size();
        return new float[] {
                Mth.lerp(progress, colors.get(colorIndex)[0], colors.get(tooLarge ? 0 : colorIndex+1)[0])/255f,
                Mth.lerp(progress, colors.get(colorIndex)[1], colors.get(tooLarge ? 0 : colorIndex+1)[1])/255f,
                Mth.lerp(progress, colors.get(colorIndex)[2], colors.get(tooLarge ? 0 : colorIndex+1)[2])/255f
        };
    }
}
