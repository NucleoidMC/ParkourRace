package me.WesleyH.parkour_race.game;

import me.WesleyH.parkour_race.game.map.ParkourRaceMapEffectConfig;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.map_templates.BlockBounds;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
import me.WesleyH.parkour_race.ParkourRace;
import me.WesleyH.parkour_race.game.map.ParkourRaceMap;

import java.util.HashMap;
import java.util.List;

public class ParkourRaceSpawnLogic {
    private final ParkourRaceMap map;
    private final ServerWorld world;
    private final List<ParkourRaceMapEffectConfig> effects;

    public final HashMap<ServerPlayerEntity, BlockBounds> playerCurCheckpoint= new HashMap<>();

    public ParkourRaceSpawnLogic(ServerWorld world, ParkourRaceMap map, List<ParkourRaceMapEffectConfig> effects) {
        this.map = map;
        this.world = world;
        this.effects = effects;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.changeGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                StatusEffectInstance.INFINITE,
                1,
                true,
                false
        ));

        for (ParkourRaceMapEffectConfig effect : effects){
            StatusEffect statusEffect = Registries.STATUS_EFFECT.get(effect.effect());
            if (statusEffect != null) {
                if (effect.duration() == 0){
                    player.addStatusEffect(new StatusEffectInstance(statusEffect,
                            StatusEffectInstance.INFINITE,
                            effect.amplifier(),
                            true,
                            false));
                }else{
                    player.addStatusEffect(new StatusEffectInstance(statusEffect,
                            effect.duration(),
                            effect.amplifier(),
                            true,
                            false));
                }

            }
        }


    }

    public void spawnPlayer(ServerPlayerEntity player) {
        // TODO: Investigate
        BlockBounds spawn = this.map.spawn;
        if (spawn == null) {
            ParkourRace.LOGGER.error("Cannot spawn player! No spawn is defined in the map!");
            return;
        }
        double x;
        double y;
        double z;
        if (playerCurCheckpoint.get(player) == null){
            x = (spawn.max().getX() + spawn.min().getX()) /2.0;
            z = (spawn.max().getZ() + spawn.min().getZ()) /2.0;
            y = spawn.max().getY();
        }else{
            BlockBounds checkpoint = playerCurCheckpoint.get(player);
            x = (checkpoint.max().getX() + checkpoint.min().getX()) /2.0;
            z = (checkpoint.max().getZ() + checkpoint.min().getZ()) /2.0;
            y = checkpoint.max().getY();
        }
        player.teleport(this.world, x, y, z, player.getYaw(), player.getPitch());
    }
}
