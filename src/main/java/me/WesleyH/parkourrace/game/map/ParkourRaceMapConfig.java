package me.WesleyH.parkourrace.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.WesleyH.parkourrace.game.ParkourRaceConfig;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;

public record ParkourRaceMapConfig(Identifier id) {
    public static final Codec<ParkourRaceMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(ParkourRaceMapConfig::id)
    ).apply(instance, ParkourRaceMapConfig::new));

}
