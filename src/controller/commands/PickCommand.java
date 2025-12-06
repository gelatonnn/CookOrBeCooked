package controller.commands;

import model.engine.GameEngine;
import model.chef.Chef;
import model.world.WorldMap;
import utils.Position;

public class PickCommand implements Command {
    private final GameEngine engine;
    private final Chef chef;
    private final WorldMap world;

    public PickCommand(GameEngine engine, Chef chef, WorldMap world) {
        this.engine = engine;
        this.chef = chef;
        this.world = world;
    }

    @Override
    public void execute() {
        Position facing = chef.getFacingPosition();
        engine.pickAt(chef, facing);
    }
}