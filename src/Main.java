package main;

import controller.GameController;
import factory.ItemRegistryInit;
import model.chef.Chef;
import model.engine.GameEngine;
import model.orders.OrderManager;
import model.world.WorldMap;
import view.gui.GamePanel;
import view.gui.HUDPanel;
import view.gui.HomePanel; // Import panel baru

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Main {
    // Komponen GUI Utama dishoimpan sebagai field agar bisa diakses antar method
    private static JFrame window;
    private static JPanel mainContainer;
    private static CardLayout cardLayout;
    private static JPanel gameContainerPanel; // Wadah untuk HUD + GamePanel
    private static GameEngine engine; // Engine disimpan agar tidak double-start

    public static void main(String[] args) {
        // 1. Inisialisasi Registry Item (Wajib di awal)
        ItemRegistryInit.registerAll();

        // 2. Setup GUI Dasar (Window & Layout)
        SwingUtilities.invokeLater(() -> {
            setupMainWindow();
            showHomeScreen();
        });
    }

    private static void setupMainWindow() {
        window = new JFrame("Nimonscooked");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        // Ukuran awal window (sebelum game diload)
        window.setSize(800, 600); 
        window.setLocationRelativeTo(null);

        // Setup CardLayout sebagai manajer tampilan utama
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        window.add(mainContainer);
    }

    private static void showHomeScreen() {
        // Buat HomePanel.
        // Kita berikan "aksi" apa yang harus dilakukan saat tombol PLAY ditekan.
        HomePanel homePanel = new HomePanel(() -> {
            // Aksi saat PLAY ditekan:
            initAndStartGame(); // 1. Siapkan game logic
            cardLayout.show(mainContainer, "GAME_SCREEN"); // 2. Ganti tampilan ke game
            // Request fokus agar keyboard langsung terdeteksi di game panel
            gameContainerPanel.requestFocusInWindow(); 
        });

        mainContainer.add(homePanel, "HOME_SCREEN");
        cardLayout.show(mainContainer, "HOME_SCREEN"); // Tampilkan Home duluan
        window.setVisible(true);
    }

    private static void initAndStartGame() {
        if (engine != null) return; 

        System.out.println("Initializing Game Engine & Views...");

        WorldMap world = new WorldMap();
        OrderManager orders = new OrderManager(false);
        engine = new GameEngine(world, orders, 180); 

        Chef c1 = new Chef("c1", "Gordon", 2, 3);
        Chef c2 = new Chef("c2", "Ramsay", 11, 6);
        engine.addChef(c1);
        engine.addChef(c2);

        GameController controller = new GameController(engine);

        gameContainerPanel = new JPanel(new BorderLayout());
        
        GamePanel gamePanel = new GamePanel(engine);
        
        // --- UPDATED: Pass callback untuk tombol EXIT ---
        HUDPanel hudPanel = new HUDPanel(engine, () -> {
            // Aksi saat tombol Exit ditekan:
            stopGame(); // 1. Matikan engine
            cardLayout.show(mainContainer, "HOME_SCREEN"); // 2. Balik ke menu
        });

        engine.addObserver(gamePanel);
        engine.addObserver(hudPanel);

        gameContainerPanel.add(hudPanel, BorderLayout.NORTH);
        gameContainerPanel.add(gamePanel, BorderLayout.CENTER);

        setupKeyListener(gameContainerPanel, controller);

        mainContainer.add(gameContainerPanel, "GAME_SCREEN");
        window.pack(); 
        window.setLocationRelativeTo(null); 

        new Thread(engine::start).start();
    }

    private static void setupKeyListener(JPanel panel, GameController controller) {
        // Agar panel bisa menerima input keyboard, dia harus bisa fokus
        panel.setFocusable(true);
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char key = Character.toLowerCase(e.getKeyChar());
                if (key == 'w' || key == 'a' || key == 's' || key == 'd')
                    controller.handleInput(String.valueOf(key));
                if (key == 'p') controller.handleInput("p");
                if (key == 'e') controller.handleInput("e");
                if (key == 'o') controller.handleInput("o");
                if (key == 't') controller.handleInput("t");
                if (e.getKeyCode() == KeyEvent.VK_TAB || key == 'c') {
                    controller.handleInput("tab");
                }
            }
        });
    }
    // --- METHOD BARU: Membersihkan Game saat Exit ---
    private static void stopGame() {
        if (engine != null) {
            engine.stop(); // Pastikan Anda punya method stop() di GameEngine yang set isRunning = false
            engine = null; // Hapus referensi agar bisa di-new lagi nanti
        }
        
        // Hapus panel game lama dari container agar tidak menumpuk memori
        // Saat play lagi, kita akan buat panel baru
        if (gameContainerPanel != null) {
            mainContainer.remove(gameContainerPanel);
            gameContainerPanel = null;
        }
    }
}