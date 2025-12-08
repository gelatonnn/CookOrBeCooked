package controller;

import java.util.List;
import model.chef.Chef;
import model.engine.GameEngine;
import utils.Direction;

public class GameController {
    private final GameEngine engine;
    private int activeChefIndex = 0;

    public GameController(GameEngine engine) {
        this.engine = engine;
    }

    public void handleInput(String key) {
        List<Chef> chefs = engine.getChefs();
        if (chefs.isEmpty()) return;

        Chef activeChef = chefs.get(activeChefIndex);

        switch (key.toLowerCase()) {
            // Standard Movement
            case "w" -> engine.moveChef(activeChef, Direction.UP);
            case "s" -> engine.moveChef(activeChef, Direction.DOWN);
            case "a" -> engine.moveChef(activeChef, Direction.LEFT);
            case "d" -> engine.moveChef(activeChef, Direction.RIGHT);

            // NEW: Diagonal Movement Inputs
            case "wa" -> engine.moveChef(activeChef, Direction.UP_LEFT);
            case "wd" -> engine.moveChef(activeChef, Direction.UP_RIGHT);
            case "sa" -> engine.moveChef(activeChef, Direction.DOWN_LEFT);
            case "sd" -> engine.moveChef(activeChef, Direction.DOWN_RIGHT);

            // Actions
            case "e" -> engine.interactAt(activeChef, activeChef.getFacingPosition());
            case "p" -> engine.pickAt(activeChef, activeChef.getFacingPosition());
            case "o" -> engine.placeAt(activeChef, activeChef.getFacingPosition());
            case "t" -> engine.throwItem(activeChef);

            // NEW: Dash Input
            case "space", "shift" -> engine.dashChef(activeChef);

            // Switch Chef
            case "c", "tab" -> {
                activeChefIndex = (activeChefIndex + 1) % chefs.size();
                System.out.println("Switched to Chef " + (activeChefIndex + 1));
            }
        }
    }
}