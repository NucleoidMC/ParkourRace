package me.WesleyH.parkourrace.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParkourRaceMap {
    private final MapTemplate template;
    private final ParkourRaceMapConfig config;
    public BlockBounds spawn;
    public ArrayList<BlockBounds> checkpoints = new ArrayList<>();
    private final BlockBounds bounds;

    public ParkourRaceMap(MapTemplate template, ParkourRaceMapConfig config) {
        this.template = template;
        this.config = config;
        this.bounds = template.getBounds();
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }

    public BlockBounds getBounds() {
        return this.bounds;
    }
}
