package com.redpxnda.respawnobelisks.registry.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.Optional;

public class NetherLandStructures extends Structure {
    public static final Codec<NetherLandStructures> CODEC = RecordCodecBuilder.<NetherLandStructures>mapCodec(instance ->
            instance.group(NetherLandStructures.settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                    Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter)
            ).apply(instance, NetherLandStructures::new)).codec();

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    public NetherLandStructures(StructureSettings config,
                                Holder<StructureTemplatePool> startPool,
                                Optional<ResourceLocation> startJigsawName,
                                int size,
                                HeightProvider startHeight,
                                Optional<Heightmap.Types> projectStartToHeightmap,
                                int maxDistanceFromCenter)
    {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    private static boolean extraSpawningChecks(GenerationContext context) {
        ChunkPos chunkpos = context.chunkPos();

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(); // mutable block pos, so its modify-able
        mutable.set(chunkpos.getMiddleBlockX(), 25, chunkpos.getMiddleBlockZ()); // setting block pos loc
        int maxHeight = 100; // max height for structure is y=100
        NoiseColumn blockView = context.chunkGenerator().getBaseColumn(mutable.getX(), mutable.getZ(), context.heightAccessor(), context.randomState()); // chunk column, for detection

        while (mutable.getY() < maxHeight) {
            BlockState state = blockView.getBlock(mutable.getY());
            BlockState upperState = blockView.getBlock(mutable.getY()+1);
            BlockState upperUpperState = blockView.getBlock(mutable.getY()+1);
            if (!state.isAir() && upperState.isAir() && upperUpperState.isAir()) return true;
            mutable.move(Direction.UP);
        }

        return false;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(); // mutable block pos, so its modify-able
        mutable.set(chunkPos.getMinBlockX(), 25, chunkPos.getMinBlockZ()); // setting block pos loc
        int maxHeight = 100; // max height for structure is y=100
        NoiseColumn blockView = context.chunkGenerator().getBaseColumn(mutable.getX(), mutable.getZ(), context.heightAccessor(), context.randomState()); // chunk column, for detection

        int startY = -1;
        while (mutable.getY() < maxHeight) {
            BlockState state = blockView.getBlock(mutable.getY());
            boolean isAirAbove = true;
            for (int i = 1; i < 8; i++) {
                if (!blockView.getBlock(mutable.getY()+i).isAir()) {
                    isAirAbove = false;
                    break;
                }
            }
            if ((!state.isAir() && !state.is(Blocks.LAVA)) && isAirAbove) {
                startY = mutable.getY()+2;
                break;
            }
            mutable.move(Direction.UP);
        }
        if (startY == -1) return Optional.empty();

        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ());

        return JigsawPlacement.addPieces(
                context, // Used for JigsawPlacement to get all the proper behaviors done.
                this.startPool, // The starting pool to use to create the structure layout from
                this.startJigsawName, // Can be used to only spawn from one Jigsaw block. But we don't need to worry about this.
                this.size, // How deep a branch of pieces can go away from center piece. (5 means branches cannot be longer than 5 pieces from center piece)
                blockPos, // Where to spawn the structure.
                false, // "useExpansionHack" This is for legacy villages to generate properly. You should keep this false always.
                Optional.empty(), // Adds the terrain height's y value to the passed in blockpos's y value. (This uses WORLD_SURFACE_WG heightmap which stops at top water too)
                // Here, blockpos's y value is 60 which means the structure spawn 60 blocks above terrain height.
                // Set this to false for structure to be place only at the passed in blockpos's Y value instead.
                // Definitely keep this false when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.
                this.maxDistanceFromCenter);
    }

    @Override
    public StructureType<?> type() {
        return ModRegistries.NETHER_STRUCTURES.get();
    }
}
