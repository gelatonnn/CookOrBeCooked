package controller.commands;

import model.chef.Chef;
import model.engine.GameEngine;
import model.world.WorldMap;
import utils.Position;

public class InteractCommand implements Command {
    private final GameEngine engine;
    private final Chef chef;
    private final WorldMap map;

    public InteractCommand(GameEngine engine, Chef chef, WorldMap map) {
        this.engine = engine;
        this.chef = chef;
        this.map = map;
    }

    @Override
    public void execute() {
        Position facing = chef.getFacingPosition();
        engine.interactAt(chef, facing);
    }
}