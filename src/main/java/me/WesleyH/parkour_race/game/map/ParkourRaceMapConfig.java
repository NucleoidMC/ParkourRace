package me.WesleyH.parkour_race.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.List;

public record ParkourRaceMapConfig(Identifier id, List<ParkourRaceMapEffectConfig> effects, boolean pvp) {
    public static final Codec<ParkourRaceMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(ParkourRaceMapConfig::id),
            ParkourRaceMapEffectConfig.CODEC.listOf().fieldOf("effects").forGetter(e -> e.effects),
            Codec.BOOL.fieldOf("pvp").forGetter(ParkourRaceMapConfig::pvp)
    ).apply(instance, ParkourRaceMapConfig::new));

}
