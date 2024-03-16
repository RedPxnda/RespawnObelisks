package com.redpxnda.respawnobelisks.registry.structure;

import com.mojang.datafixers.util.Pair;
import com.redpxnda.respawnobelisks.mixin.StructurePoolAccessor;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.List;

public class VillageAddition {
    private static final RegistryKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = RegistryKey.of(
            RegistryKeys.PROCESSOR_LIST, new Identifier("minecraft", "empty"));

    private static void addBuildingToPool(Registry<StructurePool> templatePoolRegistry,
                                          Registry<StructureProcessorList> processorListRegistry,
                                          Identifier poolRL,
                                          String nbtPieceRL,
                                          int weight) {

        RegistryEntry<StructureProcessorList> emptyProcessorList = processorListRegistry.entryOf(EMPTY_PROCESSOR_LIST_KEY);

        StructurePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) return;

        SinglePoolElement piece = SinglePoolElement.ofProcessedLegacySingle(nbtPieceRL, emptyProcessorList).apply(StructurePool.Projection.RIGID);

        for (int i = 0; i < weight; i++) {
            ((StructurePoolAccessor) pool).getElements().add(piece);
        }

        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(((StructurePoolAccessor) pool).getElementCounts());
        listOfPieceEntries.add(new Pair<>(piece, weight));
        ((StructurePoolAccessor) pool).setElementCounts(listOfPieceEntries);
    }


    public static void addNewVillageBuilding(MinecraftServer instance) {
        Registry<StructurePool> templatePoolRegistry = instance.getRegistryManager().getOptional(RegistryKeys.TEMPLATE_POOL).orElseThrow();
        Registry<StructureProcessorList> processorListRegistry = instance.getRegistryManager().getOptional(RegistryKeys.PROCESSOR_LIST).orElseThrow();
        //Village additions
        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                new Identifier("minecraft:village/plains/town_centers"),
                "respawnobelisks:village/plains_obelisk_01", 400);

        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                new Identifier("minecraft:village/taiga/town_centers"),
                "respawnobelisks:village/taiga_obelisk_01", 400);

        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                new Identifier("minecraft:village/desert/town_centers"),
                "respawnobelisks:village/desert_obelisk_01", 400);

        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                new Identifier("minecraft:village/savanna/town_centers"),
                "respawnobelisks:village/savanna_obelisk_01", 400);
    }

}
