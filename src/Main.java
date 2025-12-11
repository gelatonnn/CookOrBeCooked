import controller.GameController;
import factory.ItemRegistryInit;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

    private static void setupMainWindow() {
        window = new JFrame("CookOrBeCooked");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setMinimumSize(new Dimension(800,600));
        window.setResizable(true);
        window.setLocationRelativeTo(null);
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        window.add(mainContainer);
    }

    private static void showHomeScreen() {
        AssetManager.getInstance().playBGM("bgm_menu");

        // --- UPDATE HOME PANEL ---
        HomePanel homePanel = new HomePanel(
                () -> initAndStartGame(false), // Singleplayer
                () -> initAndStartGame(true)   // Multiplayer
        );
        // -------------------------

        mainContainer.add(homePanel, "HOME_SCREEN");
        cardLayout.show(mainContainer, "HOME_SCREEN");
        window.setVisible(true);
    }

    // Tambah parameter boolean isMultiplayer
    private static void initAndStartGame(boolean isMultiplayer) {
        AssetManager.getInstance().playBGM("bgm_game");

        if (engine != null) return;

        WorldMap world = new WorldMap();
        OrderManager orders = new OrderManager(false);
        engine = new GameEngine(world, orders, 300); // 5 Menit

        engine.setOnGameEnd(() -> {
            int finalScore = orders.getScore();
            SwingUtilities.invokeLater(() -> showGameOverScreen(finalScore));
        });

        // Spawn Logic
        List<Position> spawns = world.getSpawnPoints();
        int x1 = 2, y1 = 3;
        int x2 = 11, y2 = 6;
        if (spawns.size() >= 1) { x1 = spawns.get(0).x; y1 = spawns.get(0).y; }
        if (spawns.size() >= 2) { x2 = spawns.get(1).x; y2 = spawns.get(1).y; }

        // Nama Chef Sesuai Mode
        String name1 = isMultiplayer ? "P1 (Gordon)" : "Gordon";
        String name2 = isMultiplayer ? "P2 (Ramsay)" : "Ramsay";

        Chef c1 = new Chef("c1", name1, x1, y1);
        Chef c2 = new Chef("c2", name2, x2, y2);
        engine.addChef(c1);
        engine.addChef(c2);

        // --- PASSING IS_MULTIPLAYER KE CONTROLLER ---
        GameController controller = new GameController(engine, isMultiplayer);
        // --------------------------------------------

        gameContainerPanel = new JPanel(new BorderLayout());
        GamePanel gamePanel = new GamePanel(engine);

        HUDPanel hudPanel = new HUDPanel(engine, () -> {
            stopGame();
            AssetManager.getInstance().playBGM("bgm_menu");
            cardLayout.show(mainContainer, "HOME_SCREEN");
        });

        engine.addObserver(gamePanel);
        engine.addObserver(hudPanel);

        gameContainerPanel.add(hudPanel, BorderLayout.NORTH);
        gameContainerPanel.add(gamePanel, BorderLayout.CENTER);

        setupKeyListener(gameContainerPanel, controller);

        mainContainer.add(gameContainerPanel, "GAME_SCREEN");
        cardLayout.show(mainContainer, "GAME_SCREEN");
        window.pack();
        window.setLocationRelativeTo(null);
        gameContainerPanel.requestFocusInWindow(); // PENTING: Fokus keyboard

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

    private static void showGameOverScreen(int finalScore) {
        AssetManager.getInstance().stopBGM();
        stopGame();
        GameOverPanel gameOverPanel = new GameOverPanel(finalScore, () -> {
            AssetManager.getInstance().playBGM("bgm_menu");
            cardLayout.show(mainContainer, "HOME_SCREEN");
        });
        mainContainer.add(gameOverPanel, "GAME_OVER_SCREEN");
        cardLayout.show(mainContainer, "GAME_OVER_SCREEN");
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