package me.WesleyH.parkourrace.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.WesleyH.parkourrace.game.map.ParkourRaceMapConfig;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;

public record ParkourRaceConfig
        (PlayerConfig playerConfig, ParkourRaceMapConfig mapConfig, int timeLimitSecs)
{
    public static final Codec<ParkourRaceConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(ParkourRaceConfig::playerConfig),
            ParkourRaceMapConfig.CODEC.fieldOf("map").forGetter(ParkourRaceConfig::mapConfig),
            Codec.INT.fieldOf("time_limit_secs").forGetter(ParkourRaceConfig::timeLimitSecs)
    ).apply(instance, ParkourRaceConfig::new));
}
