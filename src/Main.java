package main;

import controller.GameController;
import factory.ItemRegistryInit;
import model.engine.GameEngine;
import model.orders.OrderManager;
import model.world.WorldMap;
import model.chef.Chef;
import view.gui.GamePanel;
import view.gui.HUDPanel; // Import panel HUD yang baru dibuat

import javax.swing.*;
import java.awt.*; // <--- PENTING: Ini mengimport BorderLayout dan layout lainnya
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Main {
    public static void main(String[] args) {
        // 1. Inisialisasi Registry Item (Agar factory kenal tomato, pasta, dll)
        ItemRegistryInit.registerAll();

        // 2. Setup World & Components
        WorldMap world = new WorldMap();
        OrderManager orders = new OrderManager(false);
        
        // Setup Engine
        GameEngine engine = new GameEngine(world, orders, 180);
        
        // Spawn Chefs
        Chef c1 = new Chef("c1", "Gordon", 2, 3);
        Chef c2 = new Chef("c2", "Ramsay", 11, 6);
        engine.addChef(c1);
        engine.addChef(c2);

        // 3. Setup Controller
        GameController controller = new GameController(engine);

        // 4. GUI Setup
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Nimonscooked - Milestone 2");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);
            
            // Gunakan BorderLayout: 
            // - NORTH (Atas) untuk HUD (Skor/Timer)
            // - CENTER (Tengah) untuk Game Area
            window.setLayout(new BorderLayout());
            
            GamePanel gamePanel = new GamePanel(engine);
            HUDPanel hudPanel = new HUDPanel(engine); 
            
            // Daftarkan panel sebagai observer agar mereka update saat game berjalan
            engine.addObserver(gamePanel);
            engine.addObserver(hudPanel); 
            
            // Pasang panel ke window
            window.add(hudPanel, BorderLayout.NORTH);
            window.add(gamePanel, BorderLayout.CENTER);
            
            window.pack(); // Sesuaikan ukuran window otomatis
            window.setLocationRelativeTo(null); // Taruh di tengah layar laptop
            window.setVisible(true);

            // Setup Input Keyboard
            window.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    char key = Character.toLowerCase(e.getKeyChar());
                    // Mapping Input
                    if (key == 'w' || key == 'a' || key == 's' || key == 'd') 
                        controller.handleInput(String.valueOf(key));
                    
                    if (key == 'p') controller.handleInput("p"); // Pick
                    if (key == 'e') controller.handleInput("e"); // Interact
                    if (key == 'o') controller.handleInput("o"); // Place / Put Down
                    if (key == 't') controller.handleInput("t"); // Throw
                    
                    // Tab atau C untuk ganti Chef
                    if (e.getKeyCode() == KeyEvent.VK_TAB || key == 'c') {
                        controller.handleInput("tab");
                    }
                }
            });
        });

        // 5. Start Game Loop
        new Thread(engine::start).start();
    }
}