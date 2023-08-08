package me.WesleyH.parkour_race;

import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import me.WesleyH.parkour_race.game.ParkourRaceConfig;
import me.WesleyH.parkour_race.game.ParkourRaceWaiting;
public class ParkourRace implements ModInitializer {

    public static final String ID = "parkourrace";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameType<ParkourRaceConfig> TYPE = GameType.register(
            new Identifier(ID, "parkour_race"),
            ParkourRaceConfig.CODEC,
            ParkourRaceWaiting::open
    );

    @Override
    public void onInitialize() {}
}
