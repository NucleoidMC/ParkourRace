package me.WesleyH.parkourrace.game;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import me.WesleyH.parkourrace.game.map.ParkourRaceMap;
import me.WesleyH.parkourrace.game.map.ParkourRaceMapGenerator;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class ParkourRaceWaiting {
    private final GameSpace gameSpace;
    private final ParkourRaceMap map;
    private final ParkourRaceConfig config;
    private final ParkourRaceSpawnLogic spawnLogic;
    private final ServerWorld world;

    private ParkourRaceWaiting(GameSpace gameSpace, ServerWorld world, ParkourRaceMap map, ParkourRaceConfig config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.world = world;
        this.spawnLogic = new ParkourRaceSpawnLogic(world, map, config.mapConfig().effects());
    }

    public static GameOpenProcedure open(GameOpenContext<ParkourRaceConfig> context) {
        ParkourRaceConfig config = context.config();
        ParkourRaceMapGenerator generator = new ParkourRaceMapGenerator(config.mapConfig());
        ParkourRaceMap map = generator.build(context.server());

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setGenerator(map.asGenerator(context.server()));

        return context.openWithWorld(worldConfig, (game, world) -> {
            ParkourRaceWaiting waiting = new ParkourRaceWaiting(game.getGameSpace(), world, map, context.config());

            GameWaitingLobby.addTo(game, config.playerConfig());

            game.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
            game.listen(GamePlayerEvents.ADD, waiting::addPlayer);
            game.listen(GamePlayerEvents.OFFER, (offer) -> offer.accept(world, Vec3d.ZERO));
            game.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
        });
    }

    private GameResult requestStart() {
        ParkourRaceActive.open(this.gameSpace, this.world, this.map, this.config);
        return GameResult.ok();
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        player.setHealth(20.0f);
        this.spawnPlayer(player);
        return ActionResult.FAIL;
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player);
    }
}
