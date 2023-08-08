package me.WesleyH.parkour_race.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record ParkourRaceMapEffectConfig(Identifier effect, int duration, int amplifier) {
    public static final Codec<ParkourRaceMapEffectConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("effect").forGetter(ParkourRaceMapEffectConfig::effect),
            Codec.INT.fieldOf("duration").forGetter(ParkourRaceMapEffectConfig::duration),
            Codec.INT.fieldOf("amplifier").forGetter(ParkourRaceMapEffectConfig::amplifier)
    ).apply(instance, ParkourRaceMapEffectConfig::new));
}
