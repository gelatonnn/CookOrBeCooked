import controller.GameController;
import factory.ItemRegistryInit;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent; // Import ini memudahkan
import java.util.List;
import javax.swing.*;
import model.chef.Chef;
import model.engine.GameEngine;
import model.orders.OrderManager;
import model.world.WorldMap;
import utils.Position;
import view.gui.AssetManager;
import view.gui.GameOverPanel;
import view.gui.GamePanel;
import view.gui.HUDPanel;
import view.gui.HomePanel;

public class Main {
    private static JFrame window;
    private static JPanel mainContainer;
    private static CardLayout cardLayout;
    private static JPanel gameContainerPanel;
    private static GameEngine engine;

    public static void main(String[] args) {
        ItemRegistryInit.registerAll();
        SwingUtilities.invokeLater(() -> {
            setupMainWindow();
            showHomeScreen();
        });
    }

    private static void showGameOverScreen(int finalScore) {
        // Matikan musik game saat Game Over muncul
        AssetManager.getInstance().stopBGM(); 
        
        stopGame();
        
        GameOverPanel gameOverPanel = new GameOverPanel(finalScore, () -> {
            // AKSI TOMBOL "BACK TO MENU":
            // Nyalakan kembali musik menu sebelum pindah layar
            AssetManager.getInstance().playBGM("bgm_menu"); 
            cardLayout.show(mainContainer, "HOME_SCREEN");
        });
        
        mainContainer.add(gameOverPanel, "GAME_OVER_SCREEN");
        cardLayout.show(mainContainer, "GAME_OVER_SCREEN");
    }

    private static void setupMainWindow() {
        window = new JFrame("CookOrBeCooked");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setSize(800, 600);
        window.setLocationRelativeTo(null);
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        window.add(mainContainer);
    }

    private static void showHomeScreen() {
        // Putar musik menu saat aplikasi pertama dibuka
        AssetManager.getInstance().playBGM("bgm_menu");

        HomePanel homePanel = new HomePanel(() -> {
            initAndStartGame();
            cardLayout.show(mainContainer, "GAME_SCREEN");
            gameContainerPanel.requestFocusInWindow();
        });
        mainContainer.add(homePanel, "HOME_SCREEN");
        cardLayout.show(mainContainer, "HOME_SCREEN");
        window.setVisible(true);
    }

    private static void initAndStartGame() {
        // Ganti ke musik game saat permainan dimulai
        AssetManager.getInstance().playBGM("bgm_game");

        if (engine != null) return;

        WorldMap world = new WorldMap();
        OrderManager orders = new OrderManager(false);
        engine = new GameEngine(world, orders, 300);

        engine.setOnGameEnd(() -> {
            int finalScore = orders.getScore();
            SwingUtilities.invokeLater(() -> showGameOverScreen(finalScore));
        });

        // Setup Chef Spawns
        List<Position> spawns = world.getSpawnPoints();
        int x1 = 2, y1 = 3;
        int x2 = 11, y2 = 6;

        if (spawns.size() >= 1) {
            x1 = spawns.get(0).x;
            y1 = spawns.get(0).y;
        }
        if (spawns.size() >= 2) {
            x2 = spawns.get(1).x;
            y2 = spawns.get(1).y;
        }

        Chef c1 = new Chef("c1", "Gordon", x1, y1);
        Chef c2 = new Chef("c2", "Ramsay", x2, y2);
        engine.addChef(c1);
        engine.addChef(c2);

        GameController controller = new GameController(engine);
        gameContainerPanel = new JPanel(new BorderLayout());
        GamePanel gamePanel = new GamePanel(engine);
        
        // --- LOGIKA EXIT DI SINI ---
        HUDPanel hudPanel = new HUDPanel(engine, () -> {
            stopGame();
            // Nyalakan kembali musik menu saat tombol Exit ditekan
            AssetManager.getInstance().playBGM("bgm_menu"); 
            cardLayout.show(mainContainer, "HOME_SCREEN");
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
        panel.setFocusable(true);
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                controller.handleInput(e);
            }
        });
    }

    private static void stopGame() {
        if (engine != null) {
            engine.stop();
            engine = null;
        }
        if (gameContainerPanel != null) {
            mainContainer.remove(gameContainerPanel);
            gameContainerPanel = null;
        }
    }
}