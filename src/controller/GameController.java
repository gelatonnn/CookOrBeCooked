package controller;

import java.util.List;
import model.chef.Chef;
import model.engine.GameEngine;
import utils.Direction;

public class GameController {
    private final GameEngine engine;
    private int activeChefIndex = 0; // Tambahan untuk melacak chef mana yang aktif

    public GameController(GameEngine engine) {
        this.engine = engine;
    }

    public void handleInput(String key) {
        List<Chef> chefs = engine.getChefs();
        if (chefs.isEmpty()) return;

        // Ambil chef yang sedang aktif
        Chef activeChef = chefs.get(activeChefIndex);

        switch (key) {
            // Movement
            case "w" -> engine.moveChef(activeChef, Direction.UP);
            case "s" -> engine.moveChef(activeChef, Direction.DOWN);
            case "a" -> engine.moveChef(activeChef, Direction.LEFT);
            case "d" -> engine.moveChef(activeChef, Direction.RIGHT);

            // Actions
            case "e" -> engine.interactAt(activeChef, activeChef.getFacingPosition());
            case "p" -> engine.pickAt(activeChef, activeChef.getFacingPosition());
            case "o" -> engine.placeAt(activeChef, activeChef.getFacingPosition()); // Tambahan tombol 'o' untuk place
            case "t" -> engine.throwItem(activeChef);

            // Switch Chef (Tab atau C)
            case "c", "tab" -> {
                activeChefIndex = (activeChefIndex + 1) % chefs.size();
                System.out.println("Switched to Chef " + (activeChefIndex + 1));
            }
        }
    }
}