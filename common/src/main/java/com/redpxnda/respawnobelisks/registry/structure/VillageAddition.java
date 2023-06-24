package com.redpxnda.respawnobelisks.registry.structure;

import com.mojang.datafixers.util.Pair;
import com.redpxnda.respawnobelisks.mixin.STPAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.ArrayList;
import java.util.List;

public class VillageAddition {
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registries.PROCESSOR_LIST, new ResourceLocation("minecraft", "empty"));

    private static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry,
                                          Registry<StructureProcessorList> processorListRegistry,
                                          ResourceLocation poolRL,
                                          String nbtPieceRL,
                                          int weight) {

        Holder<StructureProcessorList> emptyProcessorList = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);

        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) return;

        SinglePoolElement piece = SinglePoolElement.legacy(nbtPieceRL, emptyProcessorList).apply(StructureTemplatePool.Projection.RIGID);

        for (int i = 0; i < weight; i++) {
            ((STPAccessor) pool).getTemplates().add(piece);
        }

        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(((STPAccessor) pool).getRawTemplates());
        listOfPieceEntries.add(new Pair<>(piece, weight));
        ((STPAccessor) pool).setRawTemplates(listOfPieceEntries);
    }


    public static void addNewVillageBuilding(MinecraftServer instance) {
        Registry<StructureTemplatePool> templatePoolRegistry = instance.registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow();
        Registry<StructureProcessorList> processorListRegistry = instance.registryAccess().registry(Registries.PROCESSOR_LIST).orElseThrow();
        //Village additions
        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                new ResourceLocation("minecraft:village/plains/town_centers"),
                "respawnobelisks:village/plains_obelisk_01", 400);

        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                new ResourceLocation("minecraft:village/taiga/town_centers"),
                "respawnobelisks:village/taiga_obelisk_01", 400);

        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                new ResourceLocation("minecraft:village/desert/town_centers"),
                "respawnobelisks:village/desert_obelisk_01", 400);

        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                new ResourceLocation("minecraft:village/savanna/town_centers"),
                "respawnobelisks:village/savanna_obelisk_01", 400);
    }

}
