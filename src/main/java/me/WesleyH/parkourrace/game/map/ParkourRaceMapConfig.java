package me.WesleyH.parkourrace.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.WesleyH.parkourrace.game.ParkourRaceConfig;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.predicate.entity.EntityEffectPredicate;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record ParkourRaceMapConfig(Identifier id, List<ParkourRaceMapEffectConfig> effects) {
    public static final Codec<ParkourRaceMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(ParkourRaceMapConfig::id),
            ParkourRaceMapEffectConfig.CODEC.listOf().fieldOf("effects").forGetter(e -> e.effects)


    ).apply(instance, ParkourRaceMapConfig::new));

}
