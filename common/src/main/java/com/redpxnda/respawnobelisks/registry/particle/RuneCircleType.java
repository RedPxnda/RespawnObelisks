package com.redpxnda.respawnobelisks.registry.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public class RuneCircleType extends ParticleType<RuneCircleType.Options> {
    public static final Codec<Options> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("time").forGetter(options -> options.time),
            Codec.INT.fieldOf("max_time").forGetter(options -> options.maxTime)
    ).apply(instance, Options::new));

    public RuneCircleType(boolean alwaysShow) {
        super(alwaysShow, Options.DESERIALIZER);
    }

    @Override
    public Codec<Options> codec() {
        return CODEC;
    }

    public record Options(int time, int maxTime) implements ParticleOptions {

        @Override
        public ParticleType<Options> getType() {
            return ModRegistries.RUNE_CIRCLE_PARTICLE.get();
        }

        @Override
        public void writeToNetwork(FriendlyByteBuf buf) {
            buf.writeInt(time);
            buf.writeInt(maxTime);
        }

        @Override
        public String writeToString() {
            return Registry.PARTICLE_TYPE.getKey(this.getType()).toString();
        }

        public static final ParticleOptions.Deserializer<Options> DESERIALIZER = new Deserializer<>() {
            @Override
            public Options fromCommand(ParticleType<Options> particleType, StringReader stringReader) throws CommandSyntaxException {
                stringReader.expect(' ');
                int time = stringReader.readInt();
                stringReader.expect(' ');
                int maxTime = stringReader.readInt();
                return new Options(time, maxTime);
            }

            @Override
            public Options fromNetwork(ParticleType<Options> particleType, FriendlyByteBuf buf) {
                return new Options(buf.readInt(), buf.readInt());
            }
        };
    }

}
