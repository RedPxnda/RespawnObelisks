package com.redpxnda.respawnobelisks.registry.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.respawnobelisks.registry.ModRegistries;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.dynamic.Codecs;
import org.joml.Vector3f;

public class RuneCircleType extends ParticleType<RuneCircleType.Options> {
    public static final Codec<Options> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("time").forGetter(options -> options.time),
            Codec.INT.fieldOf("max_time").forGetter(options -> options.maxTime),
            Codec.FLOAT.fieldOf("scale").forGetter(options -> options.scale),
            Codecs.VECTOR_3F.fieldOf("primary_color").forGetter(options -> options.primary),
            Codecs.VECTOR_3F.fieldOf("secondary_color").forGetter(options -> options.secondary)
    ).apply(instance, Options::new));

    public RuneCircleType(boolean alwaysShow) {
        super(alwaysShow, Options.DESERIALIZER);
    }

    @Override
    public Codec<Options> getCodec() {
        return CODEC;
    }

    public record Options(int time, int maxTime, float scale, Vector3f primary, Vector3f secondary) implements ParticleEffect {

        @Override
        public ParticleType<Options> getType() {
            return ModRegistries.runeCircleParticle.get();
        }

        @Override
        public void write(PacketByteBuf buf) {
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
        public String asString() {
            return Registries.PARTICLE_TYPE.getId(this.getType()).toString();
        }

        public static final ParticleEffect.Factory<Options> DESERIALIZER = new Factory<>() {
            @Override
            public Options read(ParticleType<Options> particleType, StringReader stringReader) throws CommandSyntaxException {
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
            public Options read(ParticleType<Options> particleType, PacketByteBuf buf) {
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
