package controller;

import java.util.List;
import java.awt.event.KeyEvent;
import model.chef.Chef;
import model.engine.GameEngine;
import utils.Direction;

public class GameController {
    private final GameEngine engine;
    private int activeChefIndex = 0;

    // Throttling variables
    private long lastMoveTime = 0;
    private static final long MOVE_DELAY_MS = 150; // 1 tile every 200ms

    public GameController(GameEngine engine) {
        this.engine = engine;
    }

    public void handleInput(KeyEvent e) {
        List<Chef> chefs = engine.getChefs();
        if (chefs.isEmpty()) return;

        Chef activeChef = chefs.get(activeChefIndex);
        String key = KeyEvent.getKeyText(e.getKeyCode()).toLowerCase();

        // Actions (Immediate)
        if (key.equals("e")) engine.interactAt(activeChef, activeChef.getFacingPosition());
        else if (key.equals("p")) engine.pickAt(activeChef, activeChef.getFacingPosition());
        else if (key.equals("o")) engine.placeAt(activeChef, activeChef.getFacingPosition());
        else if (key.equals("t")) engine.throwItem(activeChef);
        else if (key.equals("tab") || key.equals("c")) {
            activeChefIndex = (activeChefIndex + 1) % chefs.size();
            System.out.println("Switched to Chef " + (activeChefIndex + 1));
        }

        // Movement Logic
        else if (isMovementKey(key)) {
            // FIX: Check for CTRL modifier
            if (e.isControlDown()) {
                handleDash(activeChef, key);
            } else {
                // Throttled Movement
                long now = System.currentTimeMillis();
                if (now - lastMoveTime >= MOVE_DELAY_MS) {
                    handleMove(activeChef, key);
                    lastMoveTime = now;
                }
            }
        }
    }

    private boolean isMovementKey(String k) {
        return k.equals("w") || k.equals("a") || k.equals("s") || k.equals("d") ||
                k.equals("up") || k.equals("down") || k.equals("left") || k.equals("right");
    }

    private void handleDash(Chef chef, String key) {
        Direction dir = getDirectionFromKey(key);
        if (dir != null) {
            chef.setDirection(dir);
            engine.dashChef(chef);
        }
    }

    private void handleMove(Chef chef, String key) {
        Direction dir = getDirectionFromKey(key);
        if (dir != null) engine.moveChef(chef, dir);
    }

    private Direction getDirectionFromKey(String key) {
        return switch (key) {
            case "w", "up" -> Direction.UP;
            case "s", "down" -> Direction.DOWN;
            case "a", "left" -> Direction.LEFT;
            case "d", "right" -> Direction.RIGHT;
            default -> null;
        };
    }
}