package me.WesleyH.parkourrace.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.util.PlayerRef;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import me.WesleyH.parkourrace.game.map.ParkourRaceMap;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.*;
import java.util.stream.Collectors;

public class ParkourRaceActive {
    public final ParkourRaceConfig config;

    public final GameSpace gameSpace;
    private final ParkourRaceMap gameMap;

    private final Object2ObjectMap<PlayerRef, ParkourRacePlayer> participants;
    private final ParkourRaceSpawnLogic spawnLogic;
    private final ParkourRaceStageManager stageManager;
    private final boolean ignoreWinState;
    private final ParkourRaceTimerBar timerBar;
    private final ServerWorld world;
    private boolean gameWon = false;


    private ParkourRaceActive(GameSpace gameSpace, ServerWorld world, ParkourRaceMap map, GlobalWidgets widgets, ParkourRaceConfig config, Set<PlayerRef> participants) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.gameMap = map;
        this.spawnLogic = new ParkourRaceSpawnLogic(gameSpace, world, map);
        this.participants = new Object2ObjectOpenHashMap<>();
        this.world = world;

        for (PlayerRef player : participants) {
            this.participants.put(player, new ParkourRacePlayer());
        }

        this.stageManager = new ParkourRaceStageManager();
        this.ignoreWinState = this.participants.size() <= 1;
        this.timerBar = new ParkourRaceTimerBar(widgets);
    }

    public static void open(GameSpace gameSpace, ServerWorld world, ParkourRaceMap map, ParkourRaceConfig config) {
        gameSpace.setActivity(game -> {
            Set<PlayerRef> participants = gameSpace.getPlayers().stream()
                    .map(PlayerRef::of)
                    .collect(Collectors.toSet());
            GlobalWidgets widgets = GlobalWidgets.addTo(game);
            ParkourRaceActive active = new ParkourRaceActive(gameSpace, world, map, widgets, config, participants);

            game.setRule(GameRuleType.CRAFTING, ActionResult.FAIL);
            game.setRule(GameRuleType.PORTALS, ActionResult.FAIL);
            game.setRule(GameRuleType.PVP, ActionResult.FAIL);
            game.setRule(GameRuleType.HUNGER, ActionResult.FAIL);
            game.setRule(GameRuleType.FALL_DAMAGE, ActionResult.FAIL);
            game.setRule(GameRuleType.INTERACTION, ActionResult.FAIL);
            game.setRule(GameRuleType.BLOCK_DROPS, ActionResult.FAIL);
            game.setRule(GameRuleType.THROW_ITEMS, ActionResult.FAIL);
            game.setRule(GameRuleType.UNSTABLE_TNT, ActionResult.FAIL);

            game.listen(GameActivityEvents.ENABLE, active::onOpen);
            game.listen(GameActivityEvents.DISABLE, active::onClose);

            game.listen(GamePlayerEvents.OFFER, (offer) -> offer.accept(world, Vec3d.ZERO));
            game.listen(GamePlayerEvents.ADD, active::addPlayer);
            game.listen(GamePlayerEvents.REMOVE, active::removePlayer);

            game.listen(GameActivityEvents.TICK, active::tick);

            game.listen(PlayerDamageEvent.EVENT, active::onPlayerDamage);
            game.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
        });
    }

    private void onOpen() {
        for (PlayerRef ref : this.participants.keySet()) {
            ref.ifOnline(this.world, this::spawnParticipant);
        }
        for (ServerPlayerEntity entity : this.gameSpace.getPlayers()){
            spawnLogic.playerCurCheckpoint.put(entity, null);
        }
        this.stageManager.onOpen(this.world.getTime(), this.config);
        // TODO setup logic
    }

    private void onClose() {
        this.gameSpace.close(GameCloseReason.FINISHED);
    }

    private void addPlayer(ServerPlayerEntity player) {
        if (!this.participants.containsKey(PlayerRef.of(player))) {
            this.spawnSpectator(player);
        }
    }

    private void removePlayer(ServerPlayerEntity player) {
        this.participants.remove(PlayerRef.of(player));
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        // TODO handle damage
        return ActionResult.FAIL;
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        // TODO handle death
        this.spawnParticipant(player);
        return ActionResult.FAIL;
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player);
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.spawnLogic.spawnPlayer(player);
    }

    private void tick() {
        long time = this.world.getTime();

        ParkourRaceStageManager.IdleTickResult result = this.stageManager.tick(time, gameSpace);



        switch (result) {
            case CONTINUE_TICK:
                break;
            case TICK_FINISHED:
                return;
            case GAME_FINISHED:
                this.broadcastWin(this.checkWinResult());
                return;
            case GAME_CLOSED:
                this.gameSpace.close(GameCloseReason.FINISHED);
                return;
        }

        this.timerBar.update(this.stageManager.finishTime - time, this.config.timeLimitSecs() * 20L);


        BlockBounds mapBounds = this.gameMap.getBounds();

        for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
            if (player != null && !mapBounds.contains(player.getBlockPos())) {
                spawnLogic.spawnPlayer(player);
            }

            for (BlockBounds checkpointBound : this.gameMap.checkpoints){
                if (player != null && checkpointBound.contains(player.getBlockPos()) &&
                        (spawnLogic.playerCurCheckpoint.get(player) == null ||
                        !spawnLogic.playerCurCheckpoint.get(player).equals(checkpointBound))){
                    spawnLogic.playerCurCheckpoint.put(player, checkpointBound);
                    System.out.println(spawnLogic.playerCurCheckpoint);
                    player.sendMessage(Text.literal("Checkpoint " + checkpointBound.toString()));
                }
            }

            if (player != null && gameMap.finish.contains(player.getBlockPos()) && !gameWon) {
                player.sendMessage(Text.literal("You win!"));
                this.broadcastWin(new WinResult(player, true));
            }
        }



    }

    private void broadcastWin(WinResult result) {
        ServerPlayerEntity winningPlayer = result.getWinningPlayer();

        Text message;
        if (winningPlayer != null) {
            message = winningPlayer.getDisplayName().copy().append(" has won the game!").formatted(Formatting.GOLD);
        } else {
            message = Text.literal("The game ended, but nobody won!").formatted(Formatting.GOLD);
        }

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.playSound(SoundEvents.ENTITY_VILLAGER_YES);
        onClose();

    }

    private WinResult checkWinResult() {
        // for testing purposes: don't end the game if we only ever had one participant
        if (this.ignoreWinState) {
            return WinResult.no();
        }

        ServerPlayerEntity winningPlayer = null;

        // TODO win result logic
        return WinResult.no();
    }

    static class WinResult {
        final ServerPlayerEntity winningPlayer;
        final boolean win;

        private WinResult(ServerPlayerEntity winningPlayer, boolean win) {
            this.winningPlayer = winningPlayer;
            this.win = win;
        }

        static WinResult no() {
            return new WinResult(null, false);
        }

        static WinResult win(ServerPlayerEntity player) {
            return new WinResult(player, true);
        }

        public boolean isWin() {
            return this.win;
        }

        public ServerPlayerEntity getWinningPlayer() {
            return this.winningPlayer;
        }
    }
}
