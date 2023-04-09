package com.redpxnda.respawnobelisks.registry.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class RuneCircleType extends ParticleType<RuneCircleType.Options> {
    public static final Codec<Options> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("time").forGetter(options -> options.time),
            Codec.INT.fieldOf("max_time").forGetter(options -> options.maxTime),
            Codec.FLOAT.fieldOf("scale").forGetter(options -> options.scale),
            Vector3f.CODEC.fieldOf("primary_color").forGetter(options -> options.primary),
            Vector3f.CODEC.fieldOf("secondary_color").forGetter(options -> options.secondary)
    ).apply(instance, Options::new));

    public RuneCircleType(boolean alwaysShow) {
        super(alwaysShow, Options.DESERIALIZER);
    }

    @Override
    public Codec<Options> codec() {
        return CODEC;
    }

    public record Options(int time, int maxTime, float scale, Vector3f primary, Vector3f secondary) implements ParticleOptions {

        @Override
        public ParticleType<Options> getType() {
            return ModRegistries.RUNE_CIRCLE_PARTICLE.get();
        }

        @Override
        public void writeToNetwork(FriendlyByteBuf buf) {
            buf.writeInt(time);
            buf.writeInt(maxTime);
            buf.writeFloat(scale);
            buf.writeFloat(primary.x());
            buf.writeFloat(primary.y());
            buf.writeFloat(primary.z());
            buf.writeFloat(secondary.x());
            buf.writeFloat(secondary.y());
            buf.writeFloat(secondary.z());
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

                stringReader.expect(' ');
                float scale = stringReader.readFloat();

                stringReader.expect(' ');
                float x1 = stringReader.readFloat();
                stringReader.expect(' ');
                float y1 = stringReader.readFloat();
                stringReader.expect(' ');
                float z1 = stringReader.readFloat();

                stringReader.expect(' ');
                float x2 = stringReader.readFloat();
                stringReader.expect(' ');
                float y2 = stringReader.readFloat();
                stringReader.expect(' ');
                float z2 = stringReader.readFloat();

                return new Options(time, maxTime, scale, new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2));
            }

            @Override
            public Options fromNetwork(ParticleType<Options> particleType, FriendlyByteBuf buf) {
                return new Options(
                        buf.readInt(),
                        buf.readInt(),
                        buf.readFloat(),
                        new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat()),
                        new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat()));
            }
        };
    }

}
