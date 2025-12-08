package controller.commands;

import model.engine.GameEngine;
import model.chef.Chef;
import utils.Direction;

public class DashCommand implements Command {
    private final GameEngine engine;
    private final Chef chef;
    private final Direction dir;
    private final int distance;

    public DashCommand(GameEngine engine, Chef chef, Direction dir, int distance) {
        this.engine = engine;
        this.chef = chef;
        this.dir = dir;
        this.distance = distance;
    }

    @Override
    public void execute() {
        for (int i = 0; i < distance; i++) {
            engine.moveChef(chef, dir);
        }
    }
}