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
    private final Set<Integer> pressedKeys = new HashSet<>();
    private int activeChefIndex = 0;

    public GameController(GameEngine engine, boolean isMultiplayer) {
        this.engine = engine;
        this.isMultiplayer = isMultiplayer;
    }

    public void handleKeyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
        processInput(e.getKeyCode(), true);
    }

    public void handleKeyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
        processInput(e.getKeyCode(), false);
    }

    private void processInput(int lastKey, boolean isPress) {
        List<Chef> chefs = engine.getChefs();
        if (chefs.isEmpty()) return;

        // SINGLE PLAYER SWITCH
        if (!isMultiplayer && isPress && (lastKey == KeyEvent.VK_TAB || lastKey == KeyEvent.VK_C)) {
            activeChefIndex = (activeChefIndex + 1) % chefs.size();
            System.out.println("Switched to Chef " + (activeChefIndex + 1));
            // Reset movement for previous chef
            engine.updateChefMovement(chefs.get((activeChefIndex + 1 + chefs.size() - 1) % chefs.size()), null, false);
            return;
        }

        if (isMultiplayer && chefs.size() >= 2) {
            updatePlayerState(chefs.get(0), KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D);
            updatePlayerState(chefs.get(1), KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);

            // Handle Actions (Instant trigger on press)
            if (isPress) {
                handleAction(chefs.get(0), lastKey, KeyEvent.VK_V, KeyEvent.VK_B, KeyEvent.VK_F);
                handleAction(chefs.get(1), lastKey, KeyEvent.VK_K, KeyEvent.VK_L, KeyEvent.VK_SEMICOLON);
            }
        } else {
            Chef activeChef = chefs.get(activeChefIndex);
            updatePlayerState(activeChef, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D);

            if (isPress) {
                handleAction(activeChef, lastKey, KeyEvent.VK_E, KeyEvent.VK_F, KeyEvent.VK_T);
                // Legacy P/O
                if (lastKey == KeyEvent.VK_P) engine.pickAt(activeChef, activeChef.getFacingPosition());
                if (lastKey == KeyEvent.VK_O) engine.placeAt(activeChef, activeChef.getFacingPosition());
            }
        }
    }

    private void updatePlayerState(Chef chef, int up, int down, int left, int right) {
        boolean u = pressedKeys.contains(up);
        boolean d = pressedKeys.contains(down);
        boolean l = pressedKeys.contains(left);
        boolean r = pressedKeys.contains(right);

        Direction dir = null;
        if (u && l) dir = Direction.UP_LEFT;
        else if (u && r) dir = Direction.UP_RIGHT;
        else if (d && l) dir = Direction.DOWN_LEFT;
        else if (d && r) dir = Direction.DOWN_RIGHT;
        else if (u) dir = Direction.UP;
        else if (d) dir = Direction.DOWN;
        else if (l) dir = Direction.LEFT;
        else if (r) dir = Direction.RIGHT;

        boolean isMoving = (dir != null);

        // Pass dash intent via CTRL key presence check handled in engine or here?
        // Let's keep it simple: Controller sets direction and moving state.
        // Dash logic can be triggered by action key or double tap, but for now we stick to movement.

        engine.updateChefMovement(chef, dir, isMoving);

        // Handle Dash (CTRL + Move) - Check if CTRL is held
        // Note: For simplicity in this structure, simple movement is continuous.
        // Dash is usually an "event". We can check modifier here if needed.
    }

    private void handleAction(Chef chef, int key, int interact, int pickPlace, int throwItem) {
        if (key == interact) {
            engine.interactAt(chef, chef.getFacingPosition());
        } else if (key == pickPlace) {
            if (chef.getHeldItem() == null) {
                engine.pickAt(chef, chef.getFacingPosition());
            } else {
                engine.placeAt(chef, chef.getFacingPosition());
            }
        } else if (key == throwItem) {
            engine.throwItem(chef);
        }
    }
}