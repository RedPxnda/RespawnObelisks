package com.redpxnda.respawnobelisks.registry.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class NetherLandStructures extends Structure {
    public static final Codec<NetherLandStructures> CODEC = RecordCodecBuilder.<NetherLandStructures>mapCodec(instance ->
            instance.group(NetherLandStructures.configCodecBuilder(instance),
                    StructurePool.REGISTRY_CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                    Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                    Heightmap.Type.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter)
            ).apply(instance, NetherLandStructures::new)).codec();

    private final RegistryEntry<StructurePool> startPool;
    private final Optional<Identifier> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Type> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    public NetherLandStructures(Config config,
                                RegistryEntry<StructurePool> startPool,
                                Optional<Identifier> startJigsawName,
                                int size,
                                HeightProvider startHeight,
                                Optional<Heightmap.Type> projectStartToHeightmap,
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

    @Override
    public Optional<StructurePosition> getStructurePosition(Context context) {
        ChunkPos chunkPos = context.chunkPos();

        BlockPos.Mutable mutable = new BlockPos.Mutable(); // mutable block pos, so its modify-able
        mutable.set(chunkPos.getStartX(), 25, chunkPos.getStartZ()); // setting block pos loc
        int maxHeight = 100; // max height for structure is y=100
        VerticalBlockSample blockView = context.chunkGenerator().getColumnSample(mutable.getX(), mutable.getZ(), context.world(), context.noiseConfig()); // chunk column, for detection

        int startY = -1;
        while (mutable.getY() < maxHeight) {
            BlockState state = blockView.getState(mutable.getY());
            boolean isAirAbove = true;
            for (int i = 1; i < 8; i++) {
                if (!blockView.getState(mutable.getY()+i).isAir()) {
                    isAirAbove = false;
                    break;
                }
            }
            if ((!state.isAir() && !state.isOf(Blocks.LAVA)) && isAirAbove) {
                startY = mutable.getY()+2;
                break;
            }
            mutable.move(Direction.UP);
        }
        if (startY == -1) return Optional.empty();

        BlockPos blockPos = new BlockPos(chunkPos.getStartX(), startY, chunkPos.getStartZ());

        return StructurePoolBasedGenerator.generate(
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
    public StructureType<?> getType() {
        return ModRegistries.netherStructureType.get();
    }
}
