package com.redpxnda.respawnobelisks.mixin;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StructurePool.class)
public interface StructurePoolAccessor {
    @Mutable
    @Accessor
    List<Pair<StructurePoolElement, Integer>> getElementCounts();

    @Mutable
    @Accessor
    ObjectArrayList<StructurePoolElement> getElements();

    @Mutable
    @Accessor
    void setElementCounts(List<Pair<StructurePoolElement, Integer>> templates);

    @Mutable
    @Accessor
    void setElements(ObjectArrayList<StructurePoolElement> templates);
}
