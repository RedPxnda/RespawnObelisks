package com.redpxnda.respawnobelisks.mixin;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StructureTemplatePool.class)
public interface STPAccessor {
    @Accessor
    List<Pair<StructurePoolElement, Integer>> getRawTemplates();

    @Accessor
    ObjectArrayList<StructurePoolElement> getTemplates();

    @Accessor
    void setRawTemplates(List<Pair<StructurePoolElement, Integer>> templates);

    @Accessor
    void setTemplates(ObjectArrayList<StructurePoolElement> templates);
}
