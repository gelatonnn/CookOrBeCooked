package controller.commands;

import model.engine.GameEngine;
import model.chef.Chef;
import utils.Direction;

public class MoveCommand implements Command {
    private final GameEngine engine;
    private final Chef chef;
    private final Direction dir;

    public MoveCommand(GameEngine engine, Chef chef, Direction dir) {
        this.engine = engine;
        this.chef = chef;
        this.dir = dir;
    }

    @Override
    public void execute() {
        engine.moveChef(chef, dir);
    }
}