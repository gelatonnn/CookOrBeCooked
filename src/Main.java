package main;

import controller.GameController;
import factory.ItemRegistryInit;
import model.engine.GameEngine;
import model.orders.OrderManager;
import model.world.WorldMap;
import model.chef.Chef;
import view.gui.GamePanel;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Main {
    public static void main(String[] args) {
        ItemRegistryInit.registerAll();

        // 1. Setup World & Components
        WorldMap world = new WorldMap();
        OrderManager orders = new OrderManager(false);
        
        // 2. Setup Engine (Manual, bukan singleton getInstance)
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
            JFrame window = new JFrame("Nimonscooked GUI");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            GamePanel panel = new GamePanel(engine);
            engine.addObserver(panel); // Daftarkan panel sebagai observer
            
            window.add(panel);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            window.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    char key = Character.toLowerCase(e.getKeyChar());
                    // Mapping sederhana
                    if (key == 'w' || key == 'a' || key == 's' || key == 'd') controller.handleInput(String.valueOf(key));
                    if (key == 'p') controller.handleInput("p");
                    if (key == 'e') controller.handleInput("e");
                    if (key == 't') controller.handleInput("t");
                }
            });
        });

        // 5. Start Loop
        new Thread(engine::start).start();
    }
}