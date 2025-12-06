package controller.commands;

import model.engine.GameEngine;
import model.chef.Chef;
import model.world.WorldMap;
import utils.Position;

public class PlaceCommand implements Command {
    private final GameEngine engine;
    private final Chef chef;
    private final WorldMap map;

    public PlaceCommand(GameEngine engine, Chef chef, WorldMap map) {
        this.engine = engine;
        this.chef = chef;
        this.map = map;
    }

    @Override
    public void execute() {
        Position facing = chef.getFacingPosition();
        engine.placeAt(chef, facing);
    }
}