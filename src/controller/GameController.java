package controller;

import java.awt.event.KeyEvent;
import java.util.List;
import model.chef.Chef;
import model.engine.GameEngine;
import utils.Direction;

public class GameController {
    private final GameEngine engine;
    private int activeChefIndex = 0;
    private final boolean isMultiplayer; // Flag Mode

    // Throttling movement
    private long lastMoveTimeP1 = 0;
    private long lastMoveTimeP2 = 0;
    private static final long MOVE_DELAY_MS = 150; 

    // Konstruktor menerima mode
    public GameController(GameEngine engine, boolean isMultiplayer) {
        this.engine = engine;
        this.isMultiplayer = isMultiplayer;
    }

    public void handleInput(KeyEvent e) {
        List<Chef> chefs = engine.getChefs();
        if (chefs.isEmpty()) return;

        int code = e.getKeyCode();
        
        if (isMultiplayer) {
            // --- MODE MULTIPLAYER (Split Controls) ---
            if (chefs.size() < 2) return;
            Chef p1 = chefs.get(0); // Chef Kiri (Gordon)
            Chef p2 = chefs.get(1); // Chef Kanan (Ramsay)

            // Input Player 1 (WASD)
            handlePlayerInput(p1, code, 
                KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, // Move
                KeyEvent.VK_V, // Interact
                KeyEvent.VK_B, // Pick/Place (Smart Button)
                KeyEvent.VK_F, // Throw
                e.isControlDown(), 1
            );

            // Input Player 2 (ARROWS)
            handlePlayerInput(p2, code, 
                KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, // Move
                KeyEvent.VK_K, // Interact
                KeyEvent.VK_L, // Pick/Place (Smart Button)
                KeyEvent.VK_SEMICOLON, // Throw (Titik Koma)
                e.isControlDown(), 2
            );

        } else {
            // --- MODE SINGLEPLAYER (Switch Logic) ---
            Chef activeChef = chefs.get(activeChefIndex);
            
            // Switch Chef Logic (TAB/C)
            if (code == KeyEvent.VK_TAB || code == KeyEvent.VK_C) {
                activeChefIndex = (activeChefIndex + 1) % chefs.size();
                System.out.println("Switched to Chef " + (activeChefIndex + 1));
                return;
            }

            // Input Active Chef (WASD)
            handlePlayerInput(activeChef, code, 
                KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D,
                KeyEvent.VK_E, // Interact
                KeyEvent.VK_F, // Pick/Place (Smart Button)
                KeyEvent.VK_T, // Throw
                e.isControlDown(), 1
            );
            
            // Support Legacy Keys (P/O) untuk Singleplayer jika masih mau dipakai
            if (code == KeyEvent.VK_P) engine.pickAt(activeChef, activeChef.getFacingPosition());
            if (code == KeyEvent.VK_O) engine.placeAt(activeChef, activeChef.getFacingPosition());
        }
    }

    private void handlePlayerInput(Chef chef, int key, 
                                   int up, int down, int left, int right,
                                   int interact, int pickPlace, int throwItem,
                                   boolean isCtrl, int playerNum) {
        
        // 1. Movement
        Direction dir = null;
        boolean isDrunk = model.engine.EffectManager.getInstance().isDrunk();
        if (key == up) dir = Direction.UP;
        else if (key == down) dir = Direction.DOWN;
        else if (key == left) dir = Direction.LEFT;
        else if (key == right) dir = Direction.RIGHT;

        if (dir != null) {
            long now = System.currentTimeMillis();
            long lastTime = (playerNum == 1) ? lastMoveTimeP1 : lastMoveTimeP2;
            // --- LOGIC BARU: THE FLASH ---
            boolean isFlash = model.engine.EffectManager.getInstance().isFlash();
            long currentDelay = isFlash ? 75 : MOVE_DELAY_MS;
            
            if (isCtrl || (isFlash && isCtrl)) {
                chef.setDirection(dir);
                engine.dashChef(chef);
            } else if (now - lastTime >= MOVE_DELAY_MS) {
                engine.moveChef(chef, dir);
                if (playerNum == 1) lastMoveTimeP1 = now;
                else lastMoveTimeP2 = now;
            }
            return;
        }

        // 2. Actions
        if (key == interact) {
            engine.interactAt(chef, chef.getFacingPosition());
        } 
        else if (key == pickPlace) {
            // SMART LOGIC: Pick or Place based on hand
            // (Memenuhi spesifikasi tabel Pick Up / Drop)
            if (chef.getHeldItem() == null) {
                engine.pickAt(chef, chef.getFacingPosition());
            } else {
                engine.placeAt(chef, chef.getFacingPosition());
            }
        } 
        else if (key == throwItem) {
            engine.throwItem(chef);
        }
    }
}