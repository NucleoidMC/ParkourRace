package me.WesleyH.parkour_race.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.game.GameOpenException;

import java.io.IOException;
import java.util.ArrayList;

public class ParkourRaceMapGenerator {

    private final ParkourRaceMapConfig config;

    public ParkourRaceMapGenerator(ParkourRaceMapConfig config) {
        this.config = config;
    }

    public ParkourRaceMap build(MinecraftServer server) throws GameOpenException {
        try {
            var template = MapTemplateSerializer.loadFromResource(server, this.config.id());
            ParkourRaceMap map = new ParkourRaceMap(template, this.config);
            map.spawn = template.getMetadata().getFirstRegionBounds("spawn");
            map.checkpoints = new ArrayList<>();
            boolean done = false;
            int i = 1;
            while (!done){
                BlockBounds checkpoint = template.getMetadata().getFirstRegionBounds("checkpoint_" + i);
                if (checkpoint != null){
                    map.checkpoints.add(checkpoint);
                    i++;
                }else {
                    done = true;
                }
            }
            map.finish = template.getMetadata().getFirstRegionBounds("finish");

            map.deathBounds = template.getMetadata().getRegionBounds("death").toList();
            return map;

        }
        catch (IOException e) {
            throw new GameOpenException(Text.literal("Failed to load map"));
        }
    }

    private static BlockBounds getRegion(MapTemplate template, String name) {
        BlockBounds bounds = template.getMetadata().getFirstRegionBounds(name);
        if (bounds == null) {
            throw new GameOpenException(Text.literal(String.format("%s region not found", name)));
        }

        return bounds;
    }


}
