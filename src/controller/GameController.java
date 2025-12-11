package controller;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.chef.Chef;
import model.engine.GameEngine;
import utils.Direction;

public class GameController {
    private final GameEngine engine;
    private final boolean isMultiplayer;
    private int activeChefIndex = 0;

    // Track active keys for smooth movement
    private final Set<Integer> pressedKeys = new HashSet<>();

    public GameController(GameEngine engine, boolean isMultiplayer) {
        this.engine = engine;
        this.isMultiplayer = isMultiplayer;
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (!pressedKeys.contains(code)) {
            pressedKeys.add(code);
            // Handle actions that trigger ONCE on press (not hold)
            handleOneTimeActions(code, e.isControlDown());
        }
        updateMovement();
    }

    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
        updateMovement();
    }

    private void updateMovement() {
        List<Chef> chefs = engine.getChefs();
        if (chefs.isEmpty()) return;

        if (isMultiplayer && chefs.size() >= 2) {
            // Player 1 (WASD)
            updateChefMovement(chefs.get(0), KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D);
            // Player 2 (ARROWS)
            updateChefMovement(chefs.get(1), KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);
        } else {
            // Single Player
            Chef activeChef = chefs.get(activeChefIndex);
            updateChefMovement(activeChef, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D);
        }
    }

    private void updateChefMovement(Chef chef, int up, int down, int left, int right) {
        boolean u = pressedKeys.contains(up);
        boolean d = pressedKeys.contains(down);
        boolean l = pressedKeys.contains(left);
        boolean r = pressedKeys.contains(right);

        Direction dir = null;

        if (u && !d) {
            if (l && !r) dir = Direction.UP_LEFT;
            else if (r && !l) dir = Direction.UP_RIGHT;
            else dir = Direction.UP;
        } else if (d && !u) {
            if (l && !r) dir = Direction.DOWN_LEFT;
            else if (r && !l) dir = Direction.DOWN_RIGHT;
            else dir = Direction.DOWN;
        } else if (l && !r) {
            dir = Direction.LEFT;
        } else if (r && !l) {
            dir = Direction.RIGHT;
        }

        // Apply Drunk Effect
        if (model.engine.EffectManager.getInstance().isDrunk() && dir != null) {
            dir = invertDirection(dir);
        }

        chef.setMoveInput(dir);
    }

    private Direction invertDirection(Direction d) {
        switch(d) {
            case UP: return Direction.DOWN;
            case DOWN: return Direction.UP;
            case LEFT: return Direction.RIGHT;
            case RIGHT: return Direction.LEFT;
            case UP_LEFT: return Direction.DOWN_RIGHT;
            case UP_RIGHT: return Direction.DOWN_LEFT;
            case DOWN_LEFT: return Direction.UP_RIGHT;
            case DOWN_RIGHT: return Direction.UP_LEFT;
            default: return d;
        }
    }

    private void handleOneTimeActions(int code, boolean isCtrl) {
        List<Chef> chefs = engine.getChefs();
        if (chefs.isEmpty()) return;

        if (isMultiplayer) {
            // P1 Actions
            handleSpecificAction(chefs.get(0), code,
                    KeyEvent.VK_V, KeyEvent.VK_B, KeyEvent.VK_F, isCtrl);
            // P2 Actions
            if (chefs.size() > 1) {
                handleSpecificAction(chefs.get(1), code,
                        KeyEvent.VK_K, KeyEvent.VK_L, KeyEvent.VK_SEMICOLON, isCtrl);
            }
        } else {
            // Single Player Actions
            if (code == KeyEvent.VK_TAB || code == KeyEvent.VK_C) {
                // Stop current chef moving before switching
                chefs.get(activeChefIndex).setMoveInput(null);
                activeChefIndex = (activeChefIndex + 1) % chefs.size();
                System.out.println("Switched to Chef " + (activeChefIndex + 1));
                updateMovement(); // Update input for new chef
                return;
            }
            handleSpecificAction(chefs.get(activeChefIndex), code,
                    KeyEvent.VK_E, KeyEvent.VK_F, KeyEvent.VK_T, isCtrl);

            // Legacy keys
            if (code == KeyEvent.VK_P) engine.pickAt(chefs.get(activeChefIndex), chefs.get(activeChefIndex).getFacingPosition());
            if (code == KeyEvent.VK_O) engine.placeAt(chefs.get(activeChefIndex), chefs.get(activeChefIndex).getFacingPosition());
        }
    }

    private void handleSpecificAction(Chef chef, int code, int interact, int pickPlace, int throwItem, boolean isCtrl) {
        if (code == interact) {
            engine.interactAt(chef, chef.getFacingPosition());
        } else if (code == pickPlace) {
            if (chef.getHeldItem() == null) engine.pickAt(chef, chef.getFacingPosition());
            else engine.placeAt(chef, chef.getFacingPosition());
        } else if (code == throwItem) {
            engine.throwItem(chef);
        } else if (isCtrl) {
            // Dash Trigger (Ctrl) - Checks direction inside engine
            engine.dashChef(chef);
        }

        // Alternative Dash Trigger (Same key as interact/run if design changes)
        // Here we rely on updateMovement + isCtrl check in engine?
        // No, dash is instant trigger.
        // Let's assume DASH is mapped to CTRL or SHIFT.
        // Since main `handleInput` passed `isCtrl`, let's use it.
        // If user presses Ctrl + W (handled in updateMovement + separate Dash check?).
        // Actually, Dash is better as an "Action" (Press Shift).
        if (code == KeyEvent.VK_SHIFT) {
            engine.dashChef(chef);
        }
    }

    // Legacy support
    public void handleInput(KeyEvent e) {
        keyPressed(e);
    }
}