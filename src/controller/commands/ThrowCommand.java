package controller.commands;

import model.engine.GameEngine;
import model.chef.Chef;
import model.world.WorldMap;

public class ThrowCommand implements Command {
    private final GameEngine engine;
    private final Chef chef;
    private final WorldMap world;

    public ThrowCommand(GameEngine engine, Chef chef, WorldMap world) {
        this.engine = engine;
        this.chef = chef;
        this.world = world;
    }

    @Override
    public void execute() {
        engine.throwItem(chef);
    }
}