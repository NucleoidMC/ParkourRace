package me.WesleyH.parkourrace.game;

import me.WesleyH.parkourrace.game.map.ParkourRaceMapEffectConfig;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameSpace;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import me.WesleyH.parkourrace.ParkourRace;
import me.WesleyH.parkourrace.game.map.ParkourRaceMap;

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
        float x;
        float y;
        float z;
        if (playerCurCheckpoint.get(player) == null){
            x = (spawn.max().getX() + spawn.min().getX()) /2F;
            z = (spawn.max().getZ() + spawn.min().getZ()) /2F;
            y = spawn.max().getY() + 0.5F;
        }else{
            BlockBounds checkpoint = playerCurCheckpoint.get(player);
            x = (checkpoint.max().getX() + checkpoint.min().getX()) /2F;
            z = (checkpoint.max().getZ() + checkpoint.min().getZ()) /2F;
            y = checkpoint.max().getY() + 0.5F;
        }
        player.teleport(this.world, x, y, z, 0.0F, 0.0F);
    }
}
