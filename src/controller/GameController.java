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
            handleOneTimeActions(code);
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

    private void handleOneTimeActions(int code) {
        List<Chef> chefs = engine.getChefs();
        if (chefs.isEmpty()) return;

        if (isMultiplayer) {
            // --- PLAYER 1 CONTROLS ---
            // Interact: V, Pick/Place: B, Throw: F, Dash: SHIFT
            handleSpecificAction(chefs.get(0), code,
                    KeyEvent.VK_V, KeyEvent.VK_B, KeyEvent.VK_F, KeyEvent.VK_SHIFT);

            // --- PLAYER 2 CONTROLS ---
            if (chefs.size() > 1) {
                // Interact: K, Pick/Place: L, Throw: ; (Semicolon), Dash: ' (Quote)
                handleSpecificAction(chefs.get(1), code,
                        KeyEvent.VK_K, KeyEvent.VK_L, KeyEvent.VK_SEMICOLON, KeyEvent.VK_QUOTE);
            }
        } else {
            // --- SINGLE PLAYER CONTROLS ---
            // Switch Chef
            if (code == KeyEvent.VK_TAB || code == KeyEvent.VK_C) {
                chefs.get(activeChefIndex).setMoveInput(null); // Stop current chef
                activeChefIndex = (activeChefIndex + 1) % chefs.size();
                updateMovement();
                return;
            }

            // Interact: E, Pick/Place: F, Throw: T, Dash: SHIFT
            handleSpecificAction(chefs.get(activeChefIndex), code,
                    KeyEvent.VK_E, KeyEvent.VK_F, KeyEvent.VK_T, KeyEvent.VK_SHIFT);

            // Legacy keys (Optional)
            if (code == KeyEvent.VK_P) engine.pickAt(chefs.get(activeChefIndex), chefs.get(activeChefIndex).getFacingPosition());
            if (code == KeyEvent.VK_O) engine.placeAt(chefs.get(activeChefIndex), chefs.get(activeChefIndex).getFacingPosition());
        }
    }

    // Updated Signature: Sekarang menerima dashKey secara spesifik, bukan boolean global
    private void handleSpecificAction(Chef chef, int code, int interact, int pickPlace, int throwItem, int dashKey) {
        if (code == interact) {
            engine.interactAt(chef, chef.getFacingPosition());
        } else if (code == pickPlace) {
            if (chef.getHeldItem() == null) engine.pickAt(chef, chef.getFacingPosition());
            else engine.placeAt(chef, chef.getFacingPosition());
        } else if (code == throwItem) {
            engine.throwItem(chef);
        } else if (code == dashKey) {
            // Sekarang Dash hanya terpanggil jika tombol spesifik ditekan
            engine.dashChef(chef);
        }
    }

    // Legacy support
    public void handleInput(KeyEvent e) {
        keyPressed(e);
    }
}