package controller.commands;

import model.engine.GameEngine;

public class QuitCommand implements Command {
    private final GameEngine engine;

    public QuitCommand(GameEngine engine) {
        this.engine = engine;
    }

    @Override
    public void execute() {
        engine.stop();
    }
}